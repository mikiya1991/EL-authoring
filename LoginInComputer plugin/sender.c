#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/types.h>

#define LISTEN_PORT 2015

int main() {
	int socksender=socket(AF_INET, SOCK_DGRAM, 0);
	struct sockaddr_in server_addr;
	memset(&server_addr, 0, sizeof(server_addr));
	server_addr.sin_family=AF_INET;
	server_addr.sin_addr.s_addr=inet_addr("127.0.0.1");
	server_addr.sin_port=htons(LISTEN_PORT);
	
	char inputbuf[256],sendbuf[256]="TEMPEST";
	int sendlen;
	while(1) {
		printf("send command:");
		gets(inputbuf);
		if (strcmp(inputbuf,"stop")==0) break;
		strcpy(sendbuf+7,inputbuf);		
		sendlen=sendto(socksender, sendbuf, strlen(sendbuf), 0, 
			(struct sockaddr*)&server_addr, sizeof(server_addr));	
		printf("send %d char.\n",sendlen);
	}	
	close(socksender);
	return 0;
}
