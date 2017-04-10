# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models

# Create your models here.


class Marker(models.Model):
    user_id = models.CharField(max_length=50)
    marker_id = models.CharField(max_length=50)
    longitude = models.CharField(max_length=50, blank=True)
    latitude = models.CharField(max_length=50, blank=True)
    url = models.CharField(max_length=50, blank=True)
    talk = models.CharField(max_length=30, blank=True)
    create = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True, null=True)
    live_ending_time = models.DateTimeField(null=True)

    def __str__(self):
        return self.marker_id
