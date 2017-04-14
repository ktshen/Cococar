
### Regulations
#### Marker Prefix
- user : moving marker, providing live streaming function
- marker : Static point, providing chat room features

#### exchanging json key naming
- marker_id
- user_id
- talk
- longitude
- latitude
- url : rtmp's url

### Note before starting
#### Copy the code below to /usr/local/nginx/conf/nginx.conf
````
http{
    server {
        set $PATH /home/bruce/Cococar/cocoweb;
        listen 8000;
        location / {            
            include  uwsgi_params;
            uwsgi_pass  unix:///tmp/cococar.sock;              
        }  
        
        location /media  {
            alias $PATH/media;  
        }
        
        location /static {
            alias $PATH/static;
        }
        
        location /hls {
            root /tmp;
            allow all;
            add_header 'Access-Control-Allow-Origin' '*';
        }
    }
}

rtmp {
    server {
        listen 1935;
        chunk_size 4096;
        allow publish all;
        application live{
            live on;
            hls on;
            hls_nested on;
            hls_path /tmp/hls;
            hls_fragment 2s;
            hls_playlist_length 10s;
            hls_cleanup off;
    }
}

}
````

#### Fire the website (current directory should be cocoweb)
````
sudo uwsgi --ini uwsgi/cococar.ini
sudo /usr/local/nginx/sbin/nginx            (start nginx)

sudo /usr/local/nginx/sbin/nginx -s stop    (stop nginx)
````



