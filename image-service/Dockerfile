FROM nginx:alpine

# Copy nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Create image directory
RUN mkdir -p /data/images

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
