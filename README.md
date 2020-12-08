# neckBand
-------------------
Bluetooth_Server.c 는 라즈베리파이에 다운로드 해야함. 

라즈베리파이와 스마트폰을 블루투스 연결할 때 main 함수의 port는 rfcomm의 채널 번호를 의미하는데 이때 해당 포트 번호를 다른 프로세스에서 쓰고 있으면 연결이 되지 않습니다. 

  sudo sdptool browse local
  
을 통해서 다른 서비스 프로파일의 rfcomm 채널 번호를 알 수 있고 그것과 다르게 설정해줘야 함. 

마이크 설정은 main 함수의 hwaparam을 변경해줘야한다.

lsusb 명령어를 통해서 내가 사용하는 마이크의 기기 번호와 스펙을 알 수 있다.
card 0 , device 0 이면 "hw:2,0"에서 "hw:0,0" 으로 바꿔주면 된다.

sampling rate도 조절할 수 있는데, 사용하는 마이크가 48000hz를 지원하면 
hwparams.rate = 48000으로 변경해주면 됨.

녹음 포맷 또한 변경할 수 있다. 
SND_PCM_FORMAT_S16_LE는 16bit little endian 으로 녹음하는 것인데,
마이크가 32bit big endian을 지원한다면
SND_PCM_FORMAT_S32_BE로 변경하면 됨.

---------------------------
