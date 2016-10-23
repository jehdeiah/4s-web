<!--- 프로젝트 소개와 개발환경 구축 안내 --->

# 머리말

**4S**(Smart Storytelling Support System) 시제품을 개발하는 프로젝트입니다. 4S는 공동작업 지원을 장기 목표로 두고 웹 앱으로 개발합니다.

중간 결과물들은 아래에서 공개됩니다.

<http://4s-web.appspot.com>

## 주요 기능

다음과 같은 기능을 만들고자 합니다.

 * 등장인물, 사건 생성과 시간 구성
  * 스토리 타임라인 편집기 
  * 담화 타임라인 편집기
 * 입체적인 스토리 세계 조망
  * 카테고리 뷰
  * 인물, 사건 관계도 
 * 지식 흐름 분석
  * 지식 구조
  * 지식 흐름 그래프
  * 복잡도 평가
 * 통합적인 스토리 창작 지원 방법론
  * 데이터베이스 활용
  * 공동창작 지원 

## 일러두기

 * KAIST 지능시스템연구실에서 진행하는 4S 프로젝트는 권호창이 기획한 연구 프로젝트의 일환으로 권혁태(@jehdeiah)가 주로 개발하였으며 유병국, 정석환, 정치훈이 기여하였습니다.
 * 이 프로젝트는 2013년 9월부터 2015년 1월까지 <https://4s-web.googlecode.com/> 에서 개발하던 것을 2016년 10월 마지막 스냅샷을 GitHub로 옮겨온 것입니다. 이에 따라 프로젝트 저장소가 SVN에서 Git으로 변경되었고 이전 SVN 변경이력은 유지하지 않습니다.
 * 처음 개발환경은 이클립스 4.3(Kepler)였으나 2014년 하반기에 4.4(Luna)로 판올림하였고, github로 옮겨오면서 모든 문서에서 4.3 관련 내용은 삭제합니다.

# 개발 환경

구글 개발자 도구들을 이용하여 웹 앱으로 만드는데, 기본 언어는 Java, [Eclipse](http://eclipse.org)에서 다음 플러그인들을 이용합니다.

 * [Google Web Toolkit](http://www.gwtproject.org/) 
 * [Google App Engine](https://developers.google.com/appengine/) 
 * EGit (Git hosted by GitHub)
 * [Apache Maven](http://maven.apache.org/)  

## 개발환경 설치

 * Java 7 (SDK version 1.7 or later)
  * 윈도우:  [Java](http://java.sun.com/javase/downloades/) 누리집에서 내려받아 설치
  * 리눅스: 배포판에서 OpenJDK 1.7 이상 설치
  * 맥OSX: 응용 프로그램 > 유틸리티 > Java 환경설정에서 Java SE 7 확인 (64비트만 지원)

 * Eclipse IDE for Java EE Developers
  * 4.4 Luna를 씁니다. [내려받기](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/lunasr2)
  * 맥 사용자의 경우 올바른 자바 버전 선택을 위해 필요한 경우 파인더에서 eclipse 실행파일의 패키지 내용 보기를 한 후 Contents > MacOS > eclipse.ini 안의 requiredJavaVersion을 1.7로 변경합니다.

 * Google Plugin for Eclipse
  * [여기](https://developers.google.com/eclipse/docs/download) 설명을 따라 eclipse 버전에 맞는 플러그인을 설치합니다.
   * 플러그인과 함께 설치되는 SDK 버전은 GWT 2.6.0, GAE 1.9.15입니다. 참고로, GAE 1.9.14에서 Objectify를 쓸 때 _심각한 문제_가 생기므로 메이븐 의존성 패키지는 1.9.14로 하지 않습니다. (개발 서버 등 도구로 쓰는 것은 괜찮습니다.)
   * 현재 메이븐에서 찾아 쓰는 최신 버전은 GWT 2.6.1, GAE 1.9.15이고, 메이븐 프로젝트로 처음 저장소에 올릴 때 쓴 버전은 GWT 2.6.0, GAE 1.9.7이었습니다.
  * 패키지 선택 화면에서 Google Plugin for Eclipse, GWT Designer for GPE, SDKs를 선택하여 설치합니다.

 * [ ] Egit
  * Google code 폐쇄로 인해 github를 씁니다.
 
 * Maven Plug-in 
  * 메이븐은 이클립스 Java EE 개발자 버전을 설치하면 자동으로 포함되어 있습니다. 루나(Luna)에는 3.2 버전이 내장되어 있습니다.
  * GWT와 GAE를 위해 사용하는 플러그인은 다음과 같습니다.
   * gwt-maven-plugin (org.codehaus.mojo)
   * appengine-maven-plugin (com.google.appengine)

 * Lombok 
  * 애노테이션(annotation)으로 코드를 간결하게 쓰기 위한 라이브러리입니다.
  * 메이븐 의존성 패키지에 추가하였으나 이클립스에서 쓰기 위해서는 lombok.jar를 실행하여 이클립스에 설치를 해야 합니다. ([여기](http://projectlombok.org/features/index.html) 설명을 따라 아래 [Lombok 설치]에 방법을 적어 두었습니다.)

##  Google Code : 공동 개발

[GitHub](https://github.com/jehdeiha/4s-web.git)를 이용하여 프로젝트 관리를 합니다. 
문서 위키와 이슈 추적을 쓸 예정입니다.  UML 등 개발관련 그림 문서는 루시드차트(<www.lucidchart.com>)에서 공동 작업을 합니다. (루시드차트는 구글 계정으로 가입할 수 있고, 개인 설정에서 이메일을 학교 계정으로 넣으면 교육용 라이선스를 통해 기능 제약 없이 사용할 수 있습니다.)

메이븐으로 프로젝트 의존성 관리를 하면서 저장소에는 pom.xml과 src 디렉토리, 이클립스 프로젝트 세팅 자료만 올려둡니다. 프로젝트 설정은 가급적 이클립스 환경 변수와 상대 경로를 썼는데 일부 사용자 관련 절대 경로가 포함될 수 있으므로 각자의 개발 환경에 맞추어 변경하고, 저장소에 올릴 때는 새로운 설정 배포가 필요하지 않는 한 프로젝트 세팅은 빼고 올립니다. ~~디버그/실행 런처 파일(`GAE_LocalServer.launch`)도 형상관리에 포함되는데 여기에도 절대 경로가 포함될 수 있으므로 개발 환경에 맞게 고치도록 합니다.~~ 새 이클립스 플러그인에서는 Development Mode에서 Super Dev Mode도 지원하므로 따로 서버 설정을 할 필요가 없습니다.

아래는 맥OSX 환경에서 SVN 저장소로부터 프로젝트를 만드는 과정을 수행하면서 적은 설명입니다.

### 소스 코드 내려받기

Git 클라이언트를 쓴다면 이 프로젝트 누리집에 있는 Code 탭의 Clone 설명에 따라 내려받을 수 있습니다. 이클립스에서 소스 코드를 내려받아 프로젝트를 만들 수 있는데, 새 프로젝트를 Git으로부터 만들 수 있습니다.

 1. 새 프로젝트 만들기에서 Git을 선택하고 URL에 https://github.com/jehdeiah/4s-web.git 입력하고, 사용자 계정 정보를 입력합니다.
 2. `master` 브랜치를 선택하고 로컬 경로를 지정하여 마무리합니다.

이렇게 하면 저장소에 연결된 소스 코드가 받아지고 프로젝트가 만들어집니다. 이 과정에서 메이븐 의존성 패키지를 자동으로 내려받게 됩니다. 만약 Lombok 설치가 안 되어 있다면 getter/setter 메쏘드가 없다는 컴파일 오류가 발생할 수 있으므로 아래 설명을 따라 Lombok을 설치하면 됩니다. 

### Lombok 설치
[lombok]:
서버쪽 코드에서 편의를 위해 lombok 패키지를 사용하므로 아래 방법으로 설치를 합니다.
 1. Lombok 패키지가 메이븐 의존성에 적혀 있으므로 ~/.m2/repository/org/projectlombok/lombok/`<version>` 경로를 찾아 들어갑니다. (메이븐 로컬 저장소 경로 ~/.m2는 시스템에 따라 다를 수 있습니다.) 혹시 lombok 패키지를 이 방법으로 찾을 수 없다면 [누리집](http://projectlombok.org/)에서 jar 파일을 내려받아 실행해도 됩니다.
 2. 명령줄에서 다음을 실행합니다.
```
java -jar lombak-<version>.jar 
```
 3. 이클립스가 감지되지 않는다면 수동으로 경로를 설정하여 설치를 마무리합니다. (이렇게 하면 이클립스 경로에 lombok.jar 파일이 복사가 되며 eclipse.ini 파일에 에이전트로 추가됩니다.)

GWT 컴파일러가 애노테이션을 무시하므로 클라이언트 코드에서는 lombok을 쓸 수 없으므로 불편하더라도 이클립스 소스 생성 기능을 이용해야 합니다.


### 프로젝트 설정과 빌드

구글 SDK를 포함한 모든 의존 라이브러리를 메이븐으로 관리하고 있으나, 플러그인을 쓰기 위해 프로젝트 설정에서 구글 SDK 사용을 명시적으로 표시해야 합니다. 저장소에 올라간 프로젝트 설정 파일에 라이브러리 정보가 들어가는데 버전 충돌을 피하기 위해 Default SDK를 사용하는 것으로 설정해 두었습니다. 업데이트 등으로 버전이 안 맞을 수 있으므로 다음과 같이 기본 SDK 버전을 수정합니다.

 1. 프로젝트 속성에서 Java Build Path 화면으로 이동 후 Libraries 탭을 선택합니다. Configure SDKs...를 눌러 기본 버전을 설정합니다.
 2. App Engine SDK와 GWT SDK 항목에서 각각 Edit 버튼을 눌러 Use Default SDK를 선택하고 Configure SDKs...를 눌러서 최신 버전을 기본값으로 해둡니다. 

참고로, 플러그인과 같이 배포되는 SDK는 최신판이 아닐 수 있습니다. 실제 빌드는 메이븐으로 하므로 플러그인에서 불러 쓰는 SDK는 디버그 등 개발 도구를 실행할 때 불러 쓰는데 서로 독립적이므로 버전이 달라도 문제가 없어 보입니다. Markers 뷰에 나타나는 유효성 검증 오류를 피하기 위해 Java Build Path > Order and Export 탭에서 플러그인과 함께 설치된 App Engine SDK 라이브러리를 메이븐 의존성 위에 둡니다.

메이븐을 쓰면서 이클립스 Markers 뷰에 몇 가지 오류나 경고가 보고되기도 하지만 무시해도 됩니다. 자바 코드의 문법 오류 표시 등은 제대로 작동하며, 리비전 46 이후에서 오류는 다 해결하고 경고는 최대한 잡았습니다. 만약 get/setXXX 메쏘드 참조 오류가 많이 나온다면 Lombok 문제이므로 설치 여부를 확인합니다. 프로젝트 빌드는 Debug As...나 Run As...에서 Maven clean, Maven install을 차례로 실행하여 합니다. Clean을 하면 데이터스토어 로컬 저장소도 같이 지워지므로 특별한 일이 없는 경우 maven clean을 생략하고 maven install만 해도 됩니다. (로컬 저장소는 outputDir인 target/{module_name} 아래 WEB-INF/appengine-generated/local_db.bin 파일에 저장됩니다.)

혹시 Maven install 과정에서 오류가 난다면 Maven install을 한번 더 실행합니다. Maven clean 후 install을 할 때 간혹 그럴 수 있습니다. 그래도 계속 오류가 날 경우 JAR 파일이 깨진 경우일 수 있으므로 콘솔 뷰에서 오류가 난 패키지를 메이븐 로컬 저장소 ~/.m2/repository 아래에서 삭제합니다. 의존성 패키지를 자동으로 내려받으므로 패키지를 삭제할 때 그 패키지를 포함하는 적당한 상위 디렉토리를 지워도 됩니다.

## 디버깅하기

GWT는 자바 코드를 자바 스크립트로 변환하므로 디버깅을 위해서는 코드 변환을 실시간으로 처리하는 _코드 서버_가 필요합니다. GAE나 GWT 자체의 개발 서버(Jetty)가 웹 서버 역할을 하고, GWT 변환 부분을 코드 서버가 처리하게 됩니다.

GWT에서 기본이 된 Super Dev Mode를 이용하여 최종 웹 앱이 배포되는 자바 스크립트 형태로 디버깅할 수 있습니다. Super Dev Mode는 GWT 앱을 자바 스크립트로 돌리므로 별도의 브라우저 플러그인을 안 씁니다. 소스 맵을 쓰기 위해 현재 크롬 브라우저를 쓰는 것이 유리합니다. 

### Development Mode: Web Application

Debug As > Web Application 항목을 선택하여 개발 모드 설정을 합니다. GWT Super Dev Mode 모드를 씁니다.

**중요!!** 
 Web Application으로 디버그/실행을 처음 하면  WAR 디렉토리를 선택하라는 화면이 나옵니다. 
 이것은 개발 서버에서 불러 쓰는 WAR 구성 파일을 배포하는 곳이므로 선택한 디렉토리의  **_모든 내용을 지우고_**  WAR 빌드 결과물을 이곳으로 복사합니다. 
 불의의 피해가 없도록(=.=) 반드시 아래와 비슷한 모양의 target/{module_name-version} 경로를 선택해야 합니다. Working directory는 기본적으로 여기서 선택하는 WAR 디렉토리가 되므로 별도의 환경 설정은 하지 않아도 됩니다.
```
${workspace_loc:smart-storytelling/target/smart-storytelling-0.0.1-SNAPSHOT}
```

개발 서버가 정상적으로 동작을 하면 Development Mode 뷰에 URL이 나오는데 그것을 웹 브라우저에서 열면 됩니다. Super Dev Mode를 처음 쓰는 경우에는 코드 서버(기본적으로 localhost:9876)에 접속하여 안내에 따라 'Dev Mode On', 'Dev Mode Off' 북마크를 추가합니다.

Super Dev Mode인 경우 서버 코드는 이클립스에서, 클라이언트 코드는 브라우저 개발자 도구에서 중단점 등을 걸어서 디버깅을 합니다. 이클립스에서 디버깅을 하는 경우는 소스 코드 수정 후 maven install로 빌드하고 브라우저를 새로고침하여 변경된 내용을 반영합니다.

Super Dev Mode에서는 북마크 Dev Mode On을 누르면 모듈 이름 옆에 컴파일 버튼이 나오는데 이것을 누르면 크롬의 개발자 도구를 이용해 클라이언트 소스를 자바 코드 형태로 보며 중단점을 거는 등 디버깅을 할 수 있습니다. 다만 바로 코드 수정은 안 되고 이클립스에서 코드 수정 후 다시 빌드할 필요 없이 저장한 후에 Dev Mode Off, Dev Mode On을 차례로 하여 크롬에서 컴파일을 하면 수정된 소스가 반영됩니다. 서버 쪽 코드는 이클립스에서 바꿉니다.


### 앱 엔진 개발 서버

Development Mode에서 자동으로 GAE 개발 서버를 실행시킵니다. 기본 포트는 8888이며, 이 서버 주소 뒤에 웹 앱 시작 페이지(StoryApp.html)를 붙여 열면 웹 앱이 실행되고 `/_ah/admin`으로 열면 개발자 콘솔을 볼 수 있습니다. (여기에서 데이터스토어 내용도 확인할 수 있습니다.) 

Development Mode와는 별개로 GAE 서버를 돌릴 수 있는데, 이를 위해서는 이클립스 Java EE perspective 아래에 있는 View 화면에서 Servers 탭을 선택하고 새 서버를 추가합니다. 구글 앱 엔진을 선택하고 나머지는 기본값으로 두어도 됩니다. 계속 Next로 넘기다가 Add and Remove 창에서 smart-storytelling 모듈을 추가하고 완료합니다. 설정창의 Arguments 탭에서 실행 파라미터와 Working directory에 WAR 경로를 설정해야 하는데, 서버 속성에서 환경설정을 열 때마다 작업 디렉토리 경로가 초기화되는 문제가 있습니다. (Common 탭에서 Shared File로 환경설정 내용을 저장하면 이 문제를 조금 피할 수는 있습니다.)

### GWT 코드 서버 돌리기

 디버그나 실행 환경설정(configuration) 또는 maven build... 메뉴를 통해 maven goal을 gwt:run-codeserver로 설정하고 실행하면 코드 서버만 돌릴 수 있습니다. 앱 엔진 개발 서버를 따로 돌릴 때 유용합니다.

### 참고

디버깅을 시도하면 주소가 이미 사용중이라고 실행이 안 될 때가 있습니다. 이전 실행 프로세스가 완전히 종료되지 않아서 네트워크 포트를 점유하고 있는 것이므로 시스템 감시나 작업관리자 등으로 프로세스 목록을 살펴보고 해당 java 프로세스를 찾아서 강제로 끄면 됩니다. 


# 웹서비스

구글 앱 엔진을 이용하면 이클립스에서 바로 웹 사이트에 deploy 할 수 있습니다. 현재 주소는 <http://4s-web.appspot.com>입니다. 나중에 웹 운영을 위해서는 개인 계정이 아닌 관리용 구글 계정을 만들고 배포해야 할 것 같습니다.

## 앱 배포 (Deployment)

구글 플러그인을 사용하면 빌드 경로 등에 문제가 생기므로 메이븐을 써서 웹에 올립니다. 
웹 배포를 위해서 `mvn appengine:update` 명령을 쓰는데 디버그나 실행의 maven build... 메뉴를 통해 maven goal을 appengine:update로 설정하고 실행합니다. 

데이터베이스는 앱 엔진에서 제공하는 데이터스토어를 Objectify를 이용해서 씁니다.
