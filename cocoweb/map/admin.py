# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.contrib import admin

from .models import Marker, GPSInfo

# Register your models here.

class GPSInline(admin.TabularInline):
    model = GPSInfo

class MarkerModelAdmin(admin.ModelAdmin):
    inlines = [
        GPSInline,
    ]
    list_display = ["marker_id", "user_id", "last_longitude",
                    "last_latitude", "url", "talk", "create",
                    "live_ending_time", "modified"]

    search_fields = ["user_id", "gps__longitude", "gps__latitude"]

    def last_longitude(self, obj):
        if obj.gps.order_by('-create') is None:
            return ''
        else:
            return obj.gps.order_by('-create')[0].longitude

    def last_latitude(self, obj):
        if obj.gps.order_by('-create') is None:
            return ''
        else:
            return obj.gps.order_by('-create')[0].latitude

admin.site.register(Marker, MarkerModelAdmin)


