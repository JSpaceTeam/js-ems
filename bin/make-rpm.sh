#!/bin/bash

# script to make rpm
# it reads the source directory & creates links to the rpmbuild dir
# default is to use current directory, or specify as only argument

# arguments:
# -s: source (path) of directory to build, current dir if unspecified
# -t: topdir, top of JMP depot (directory containing CMP, etc...)

die()
{
	echo "$@"
	exit 1
}

usage()
{
	cat <<EOF
Usage: `basename $0` [flag value]...
Example: `basename $0` -rpmtop ~/CI/jenkins/jobs/BUILD_js-ems/workspace/rpmbuild -gittop ~/CI/jenkins/jobs/BUILD_js-ems/workspace/js-ems

Flags:
  -rpmtop: path to rpmbuild, no default
  -gittop: path to Top of Git repository, no default
  -m: 0 not show log; 1 show log; default 1
  -v: JSpace release number, example 11.2
  -c: JSpace commit number of GitHub
  -h: Folderlocation where help fileis are , example /tmp/help

EOF

}

while [ $# -gt 0 ]; do
	case $1 in
	"-rpmtop")
		shift
		srcdir=$1
		RPM_TOP=$1
        ;;
	"-gittop")
		shift
		GIT_TOP=$1
        ;;
    "-m")
        shift
        SHOWLOG=$1
        ;;
    "-v")
		shift
        VERSION=$1
        ;;
    "-c")
        shift
        CMT_NUM=$1
        ;;
	"-h")
        shift
        HELP=$1
        ;;
	*) echo "Unknown flag: \"$1\""
        usage
        exit 1
    ;;
    esac
    shift
done

if [ "$RPM_TOP" = "" ]; then
    die "Please set parameter -rpmtop"
else
    if [ -d $RPM_TOP ]; then
        rm -rf $RPM_TOP
    fi
    mkdir -p $RPM_TOP
    pushd $RPM_TOP >/dev/null 
    mkdir -p $RPM_TOP/{BUILD,RPMS,SPECS,SRPMS,SOURCES}
    popd >/dev/null
fi

if [ "$GIT_TOP" = "" ]; then
    die "Please set parameter -gittop"
fi


    echo "Set default parameters ......"
    # if VERSION is null use current yyyymmdd as default
    VERSION=${VERSION:-$(date +%Y%m%d)}

    # show log for default
    SHOWLOG=${SHOWLOG:-1}

    # set spec file path
    SPEC_FILE=$GIT_TOP/bin/rpmbuild.spec

    # set compiled files path
    DIST_DIR=$GIT_TOP/target/dist

    # if CMT_NUM is null use the late commit number (first 8 bit) as default
    if [ "$CMT_NUM" = "" ]; then
        pushd $GIT_TOP >/dev/null || die "Cannot find directory \"$GIT_TOP\""
        CMT_NUM=$(git log -n 1|head -1|cut -d' ' -f2)
        CMT_NUM=${CMT_NUM:0:8}
        popd >/dev/null
    fi

    echo "VERSION=$VERSION"
    echo "CMT_NUM=$CMT_NUM"
    echo "SHOWLOG=$SHOWLOG"

    
which rpmbuild >/dev/null 2>/dev/null || die "Cannot find rpmbuild, is it installed?"

# functions
cleandir()
{
	echo "Cleaning $1"
	pushd $1 >/dev/null || die "Cannot change to dir $1"
	for tf in *
	do
		if [ -L $tf ]; then
			#echo "$1/$tf: symlink"
			rm $tf
		elif [ -d $tf ]; then
			#echo "$1/$tf: directory"
			rm -rf $tf
		else
			#echo "$1/$tf: file"
			rm -f $tf
		fi
	done
	popd >/dev/null
}

#####
# main
#####

# stop on first error
set -o errexit

# clean SOURCES dir
cleandir $RPM_TOP/SOURCES
cleandir $RPM_TOP/BUILD
cleandir $RPM_TOP/SPECS

cp $SPEC_FILE $RPM_TOP/SPECS/
SPEC_FILE=$RPM_TOP/SPECS/rpmbuild.spec

if [ -n "$SPEC_FILE" ]; then
	output=$RPM_TOP/BUILD/$(basename $SPEC_FILE)
	output=${output/\.spec/.log}

	echo "Starting build, logging to $output"
	# allow errors from this point on
	set +o errexit
	rpmbuild -bb \
		--define "jmp_version_num $VERSION" \
		--define "jmp_release_num $CMT_NUM" \
		--define "_topdir $RPM_TOP" \
        --define "buildroot $RPM_TOP/rpmbuildroot" \
        --define "dist_dir $DIST_DIR" \
		--define "jmp_top $GIT_TOP" \
		$SPEC_FILE 1>>$output 2>>$output
	RV=$?
        if [ "$SHOWLOG" = "1" ]; then
	    echo "Build done, showing log"
	    notseen=1
	    which more >/dev/null 2>/dev/null
	    if [ $? -eq 0 -a -x $( which more ) ]; then
		more $output --nofork
		notseen=$?
	    fi
	    if [ $notseen -ne 0 ]; then
		less $output
	    fi
        else
            echo "Build done, showing log"
        fi
        
	if [ $RV -eq 0 ]; then
		# made RPM, find out where
		for tl in $( grep "^Wrote:" $output ); do
			echo $tl
			#tf=$( echo $tl | cut -d: -f2 )
			#ls -l $tf || echo "Can't ls: $tl"
		done
	elif [ "$(basename $(dirname $output))" = "tmp" ]; then
		rm $output
	fi
else
	die "No SPEC_FILE!"
fi

exit $RV

