###Note before starting
#### Copy the code below to /usr/local/nginx/conf/nginx.conf
````
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
    }
````

#### Fire the website (current directory should be cocoweb)
````
sudo uwsgi --ini uwsgi/cococar.ini
sudo /usr/local/nginx/sbin/nginx            (start nginx)

sudo /usr/local/nginx/sbin/nginx -s stop    (stop nginx)
````



