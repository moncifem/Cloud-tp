    #!/bin/bash

echo "Select a server to connect to using ssh:"
echo "1. Nginx-1 Server (port 2300)"
echo "2. Nginx-2 Server (port 2400)"
echo "3. Mosquitto-1 Server (port 1800)"
echo "4. Mosquitto-2 Server (port 1802)"
echo "5. Redis Server (port 222)"
echo "6. Postgres Server (port 444)"
echo "7. Mongodb Server (port 888)"
echo "8. Rabbitmq Server (port 1900)"
echo "9. Jenkins Server (port 2000)"
echo "10. Jenkins Server (port 2200)"
echo "11. Microservices Server (port 2100)"
echo "12. GUI Server (port 22)"

read -p "Enter your choice (1/2/3): " choice

case $choice in
    1)
        ssh -p 2300 kingsman@172.31.252.134
        ;;
    2)
        ssh -p 2400 kingsman@172.31.252.134
        ;;
    3)
        ssh -p 1800 kingsman@172.31.252.134
        ;;
    4)
        ssh -p 1802 kingsman@172.31.252.134
        ;;
    5)
        ssh -p 222 redis@172.31.252.134
        ;;
    6)
        ssh -p 444 postgres@172.31.252.134
        ;;
    7)
        ssh -p 888 mongo@172.31.252.134
        ;;
    8)
        ssh -p 1900 kingsman@172.31.252.134
        ;;
    9)
        ssh -p 2000 kingsman@172.31.252.134
        ;;
    10)
        ssh -p 2200 kingsman@172.31.252.134
        ;;
    11)
        ssh -p 2100 kingsman@172.31.252.134
        ;;
    12)
        ssh gui@172.31.252.126
        ;;
    *)
        echo "Invalid choice. Exiting."
        ;;
esac
