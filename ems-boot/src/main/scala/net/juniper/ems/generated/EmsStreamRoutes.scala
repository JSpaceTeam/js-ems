package net.juniper.ems.generated

import com.typesafe.scalalogging.LazyLogging
import net.juniper.easyrest.auth.EasyRestAuthenticator
import net.juniper.easyrest.notification.NotificationSubscriptionManager
import net.juniper.easyrest.rest.EasyRestRoutingDSL
import net.juniper.easyrest.rest.EasyRestServerSideEventDirective._
import net.juniper.easyrest.streams.spray.StreamRegistry
import net.juniper.easyrest.streams.yang.Stream
import spray.httpx.encoding.Gzip
import spray.routing.HttpService
import spray.routing.directives.RefFactoryMagnet

/**
 * This is the generated stream API routes
 */
trait EmsStreamRoutes extends EasyRestRoutingDSL with LazyLogging with HttpService {

   //Each stream should be registered

    StreamRegistry.registerStream(
      new Stream()
        .name("database-changes")
        .description("Stream for all database change events")
        .replaySupport("false")
        .events("")
    )

    val streamsRestApiRouting = compressResponseIfRequested(new RefFactoryMagnet()) {
      get {
        path(ROUTING_PREFIX / ROUTING_STREAMS_PREFIX / ROUTING_STREAM_PREFIX / "database-changes" / ROUTING_EVENTS_PREFIX) {
          authenticate(EasyRestAuthenticator()) { apiCtx =>
            authorize(enforce(apiCtx)) {
              intercept(apiCtx) {
                compressResponse(Gzip) {
                  sse { (channel, lastEventId, ctx) =>
                  {
                    NotificationSubscriptionManager.addSubscriber(channel, "database-changes", ctx.request.uri.query.get("stream-filter"))
                  }
                  }
                }
              }
            }
          }
        }
      }
    }
}
