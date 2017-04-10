from django.conf.urls import url
from . import views

urlpatterns = [
    url(r'^$', views.home, name='map-home'),
    url(r'^marker$', views.request_marker, name='map-marker'),
    url(r'^search$', views.search_record, name='search_record'),
    url(r'^about$', views.about, name='about')
]
