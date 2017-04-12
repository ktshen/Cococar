# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models

# Create your models here.


class Marker(models.Model):
    user_id = models.CharField(max_length=50)
    marker_id = models.CharField(max_length=50)
    url = models.CharField(max_length=50, blank=True)
    create = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True, null=True)
    live_ending_time = models.DateTimeField(null=True, blank=True)
    deleted = models.BooleanField(default=False)

    def __str__(self):
        return self.marker_id


class GPSInfo(models.Model):
    marker = models.ForeignKey(Marker, on_delete=models.CASCADE, related_name="gps")
    longitude = models.CharField(max_length=50)
    latitude = models.CharField(max_length=50)
    create = models.DateTimeField(auto_now_add=True)


class Talk(models.Model):
    marker = models.ForeignKey(Marker, on_delete=models.CASCADE, related_name="talk")
    talk = models.CharField(max_length=200)
    create = models.DateTimeField(auto_now_add=True)