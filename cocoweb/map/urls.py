from django.conf.urls import url
from . import views

urlpatterns = [
    url(r'^$', views.home, name='map-home'),
    url(r'^chat/$', views.chat, name='map-home'),
    url(r'^marker/$', views.request_marker, name='map-marker'),
]
