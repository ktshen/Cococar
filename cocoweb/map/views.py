# -*- coding: utf-8 -*-
from __future__ import unicode_literals
import json
import datetime
from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from .models import Marker


# Create your views here.
def home(request):
    return render(request, "map.html")

def chat(request):
    return render(request, "index.html")


@csrf_exempt
def request_marker(request):
    if request.method == "POST":
        rc = json.loads(request.body)
        marker_id = rc.get("marker_id")
        if marker_id is None:
            return HttpResponse(status=400)
        if "live_finish" in rc.keys():
            try:
                marker = Marker.objects.get(marker_id=marker_id)
            except Marker.DoesNotExist:
                return HttpResponse(status=400)
            marker.live_ending_time = datetime.datetime.now()
            marker.save()
            return HttpResponse(status=200)
        else:
            marker_id = rc.get("marker_id")
            try:
                marker = Marker.objects.get(marker_id=marker_id)
            except Marker.DoesNotExist:
                marker = Marker()
            for key in rc.keys():
                setattr(marker, key, rc[key])
            marker.save()
            return HttpResponse(status=200)
    else:
        queryset = Marker.objects.filter(marker_id__istartswith="marker") \
            .filter(create__gte=datetime.datetime.now() - datetime.timedelta(hours=1))
        queryset += Marker.objects.filter(marker_id__istartswith="user") \
            .exclude(live_ending_time__isnull=False)
        response = dict()
        for m in queryset:
            d = {
                "marker_id": m.marker_id,
                "user_id": m.user_id,
                "longitude": m.longitude,
                "latitude": m.latitude,
                "url": m.url,
                "talk": m.talk,
            }
            response.update(m)
        return JsonResponse(data=response)



