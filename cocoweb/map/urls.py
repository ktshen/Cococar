from django.conf.urls import url
from . import views

urlpatterns = [
    url(r'^$', views.home, name='map-home'),
    url(r'^search$', views.search_record, name='search_record'),
    url(r'^video$', views.video, name="video"),
    url(r'^about$', views.about, name='about'),
    url(r'^marker$', views.request_marker, name='map-marker'),
    url(r'^chatroom$', views.fire_chatroom, name='chatroom'),
    url(r'^chat$', views.update_chatroom, name='update-chat'),
]
