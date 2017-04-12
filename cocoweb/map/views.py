# -*- coding: utf-8 -*-
from __future__ import unicode_literals
import json
import datetime
import os
from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone
from .models import Marker, GPSInfo, Talk

m3u8_base = "http://140.115.158.81/hls"

# Create your views here.


def home(request):
    return render(request, "map.html")


def about(request):
    return render(request, "about.html")


def search_record(request):
    pass


@csrf_exempt
def video(request):
    marker = request.POST["marker_id"]
    m3u8_path = os.path.join(m3u8_base, marker, "index.m3u8")
    return render(request, "hls.html", context={"hls_url": m3u8_path})


@csrf_exempt
def request_marker(request):
    if request.method == "POST":
        rc = json.loads(request.body)
        marker_id = rc.get("marker_id")
        if marker_id is None:
            return HttpResponse(status=400)
        if "message" in rc.keys():
            if rc["message"] == "stop_live":
                try:
                    marker = Marker.objects.get(marker_id=marker_id)
                except Marker.DoesNotExist:
                    return HttpResponse(status=400)
                marker.live_ending_time = timezone.now()
                marker.save()
            elif rc["message"] == "delete":
                try:
                    marker = Marker.objects.get(marker_id=marker_id)
                    marker.deleted = True
                except Marker.DoesNotExist:
                    return HttpResponse(status=400)
        else:
            marker_id = rc.get("marker_id")
            marker = Marker.objects.get_or_create(marker_id=marker_id)[0]
            for key in rc.keys():
                if key != "longitude" or key != "latitude" or key !="talk":
                    setattr(marker, key, rc[key])
            if "longitude" in rc.keys() and "latitude" in rc.keys():
                gps = GPSInfo(marker=marker, latitude=rc["latitude"], longitude=rc["longitude"])
                gps.save()
            if "talk" in rc.keys():
                talk = Talk(marker=marker, talk=rc["talk"])
                talk.save()
            marker.save()
        return HttpResponse(status=200)
    else:
        queryset = Marker.objects.exclude(deleted=True)\
                                 .filter(marker_id__istartswith="marker") \
                                 .filter(create__gte=datetime.datetime.now() - datetime.timedelta(hours=1))
        queryset2 = Marker.objects.exclude(deleted=True)\
                                  .filter(marker_id__istartswith="user")\
                                  .exclude(live_ending_time__isnull=False)
        response = []
        for q in [queryset, queryset2]:
            for m in q:
                talk = m.talk.order_by("-create").first()
                if talk is None:
                    talk = " "
                else:
                    talk = talk.talk
                d = {
                    "marker_id": m.marker_id,
                    "user_id": m.user_id,
                    "longitude": m.gps.order_by("-create")[0].longitude,
                    "latitude": m.gps.order_by("-create")[0].latitude,
                    "url": m.url,
                    "talk": talk
                }
                response.append(d)
        return JsonResponse(data=response, safe=False)


@csrf_exempt
def chatroom(request):
    time_format = "%Y:%m:%d %H:%M:%S"
    if request.method == "POST":
        rc = json.loads(request.body)
        marker_id = rc.get("marker_id", None)
        if marker_id is None:
            return HttpResponse(status=400)
        try:
            m = Marker.objects.get(marker_id=marker_id)
        except Marker.DoesNotExist:
            return HttpResponse(status=400)
        last_time = rc.get("time", None)
        if last_time is None:
            room = m.talk.order_by('-create')
        else:
            last_time = datetime.datetime.strptime(time_format)
            room = m.talk.filter(create__gte=last_time).order_by('-create')
        response = []
        for c in room:
            response.append([c.create.strftime(time_format), c.talk])
        return JsonResponse(data=response, safe=False)





