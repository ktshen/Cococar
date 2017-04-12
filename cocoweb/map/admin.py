# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.contrib import admin

from .models import Marker, GPSInfo, Talk

# Register your models here.


class GPSInline(admin.TabularInline):
    model = GPSInfo


class TalkInline(admin.TabularInline):
    model = Talk


class MarkerModelAdmin(admin.ModelAdmin):
    inlines = [
        GPSInline,
        TalkInline
    ]
    list_display = ["marker_id", "user_id", "last_longitude",
                    "last_latitude", "url", "create",
                    "live_ending_time", "modified"]

    search_fields = ["user_id", "gps__longitude", "gps__latitude"]

    def last_longitude(self, obj):
        q = obj.gps.order_by('-create').first()
        if q is None:
            return ''
        else:
            return q.longitude

    def last_latitude(self, obj):
        q = obj.gps.order_by('-create').first()
        if q is None:
            return ''
        else:
            return q.latitude

admin.site.register(Marker, MarkerModelAdmin)


