# -*- coding: utf-8 -*-
from __future__ import unicode_literals
import json
import datetime
import os
from django.shortcuts import render, get_object_or_404
from django.template.loader import render_to_string
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone
from .models import Marker, GPSInfo, Talk
from .forms import SearchForm

m3u8_base = "http://140.115.158.81/hls"
DATE_TIME_FORMAT = "%Y/%m/%d %H:%M:%S"

# Create your views here.


def home(request):
    return render(request, "map.html", context={"search_form": SearchForm()})


def about(request):
    return render(request, "about.html")

def search_record(reqeust):
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
            marker = Marker.objects.get_or_create(marker_id=marker_id)[0]
            for key in rc.keys():
                if key not in ["longitude", "latitude", "talk"]:
                    setattr(marker, key, rc[key])
            if "longitude" in rc.keys() and "latitude" in rc.keys():
                gps = GPSInfo(marker=marker, latitude=float(rc["latitude"]), longitude=float(rc["longitude"]))
                gps.save()
            if "talk" in rc.keys() and rc["talk"]:
                talk = Talk(marker=marker, talk=rc["talk"])
                talk.save()
            marker.save()
        return HttpResponse(status=200)
    else:
        queryset = Marker.objects.exclude(deleted=True)\
                                 .filter(marker_id__istartswith="marker") \
                                 .filter(create__gte=datetime.datetime.now() - datetime.timedelta(hours=2))
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
                    "longitude": str(m.gps.order_by("-create")[0].longitude),
                    "latitude": str(m.gps.order_by("-create")[0].latitude),
                    "url": m.url,
                    "talk": talk
                }
                response.append(d)
        return JsonResponse(data=response, safe=False)


@csrf_exempt
def fire_chatroom(request):
    try:
        rc = json.loads(request.body)
    except ValueError:
        return HttpResponse(status=400)
    marker_id = rc.get("marker_id", None)
    if marker_id is None:
        return HttpResponse(status=400)
    m = get_object_or_404(Marker, marker_id=marker_id)
    room = m.talk.order_by('create')
    response = []
    for c in room:
        response.append([c.create.strftime(DATE_TIME_FORMAT), c.talk])
    html = render_to_string("chatroom.html",
                            {"allchat":response,
                             "chat_id": "chat-"+marker_id})
    d = {"html": html}
    if room.last():
        d["last_time"] = room.last().create.strftime(DATE_TIME_FORMAT)
    return JsonResponse(data=d)


@csrf_exempt
def update_chatroom(request):
    try:
        rc = json.loads(request.body)
    except ValueError:
        return HttpResponse(status=400)
    marker_id = rc.get("marker_id", None)
    last_time = rc.get("last_time", None)

    if not marker_id or not last_time:
        return HttpResponse(status=400)
    m = get_object_or_404(Marker, marker_id=marker_id)
    last_time = datetime.datetime.strptime(last_time, DATE_TIME_FORMAT)
    room = m.talk.filter(create__gte=last_time + datetime.timedelta(seconds=1)).order_by('create')
    d = {}
    if room.exists():
        response = []
        for c in room:
            response.append([c.create.strftime(DATE_TIME_FORMAT), c.talk])
        d["html"] = render_to_string("chat.html", {"allchat": response})
        if room.last():
            d["last_time"] = room.last().create.strftime(DATE_TIME_FORMAT)
    return JsonResponse(data=d)

