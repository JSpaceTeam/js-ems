package net.juniper.ems.notifications.handlers

import net.juniper.easyrest.notification.Notification
import net.juniper.yang.mo.emsNotifications.DatabaseChanges

/**
 * Wrapping Yang Generated notification to mixin Notification trait required by Notification Subsystem.
 *
 */
class DatabaseChangeNotification extends DatabaseChanges with Notification

