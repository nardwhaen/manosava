# Manosaba Pixel Dungeon — 작업 메모

Shattered Pixel Dungeon **v4.0.0** (원본 커밋 `2bb34a4`) 기반 모드 프로젝트.
원본 라이선스는 GPLv3 — 모드를 배포할 때는 소스도 GPLv3로 공개해야 함.

## 원본에서 바꾼 것 (초기 세팅)

| 파일 | 변경 |
|---|---|
| `build.gradle` | `appName` → `Manosaba Pixel Dungeon`, `appPackageName` → `com.manosaba.pixeldungeon`, 버전명 `4.0.0-mod0.1` |
| `android/build.gradle`, `desktop/build.gradle` | 릴리스 빌드도 debugUpdates / debugNews 사용 (원본 업데이트·뉴스 알림이 모드에 뜨지 않게) |
| `ios/build.gradle` | 패키지명 변경 후에도 iOS 메인 클래스 경로가 깨지지 않게 고정 |

모드 이름·앱 ID를 바꾸고 싶으면 `build.gradle` 맨 위 `appName`, `appPackageName` 두 줄만 고치면 됨.
앱 ID가 원본과 다르므로 폰에 원본 SPD와 모드를 같이 설치할 수 있고, PC 세이브 폴더도 따로 생김.

## 필요한 것

- Android Studio (최신 버전 권장 — 이 버전은 Android Gradle Plugin 9.2 / Gradle 9.5 / compileSdk 36을 씀)
- Gradle JDK: **JDK 17 이상** (Android Studio 내장 JBR 21이면 충분)
  `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`
- SDK Manager에서 **Android 15/16 (API 36) SDK Platform** 설치

## 여는 법

1. Android Studio → **Open** → `E:\Manosaba_Pixel_Dungeon\shattered-pixel-dungeon-master` 폴더 선택
2. 첫 Gradle Sync는 의존성 다운로드 때문에 몇 분 걸림
3. 실행
   - **PC에서 테스트 (가장 빠름)**: 오른쪽 Gradle 탭 → `desktop > Tasks > other > debug` 더블클릭
     (한 번 실행하면 상단 실행 구성 목록에 남아서 다음부터는 ▶만 누르면 됨. 터미널로는 `gradlew desktop:debug`)
   - **폰/에뮬레이터**: 실행 구성 `android` 선택 → 기기 선택 → ▶

## 코드 위치

- 게임 로직: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`
  - `actors/` 영웅·몬스터·버프, `items/` 아이템, `levels/` 던전 생성, `scenes/`·`windows/`·`ui/` 화면
- 텍스트(번역): `core/src/main/assets/messages/` (`*_ko.properties`가 한국어)
- 이미지·사운드: `core/src/main/assets/`
- 엔진(렌더링·입력 등): `SPD-classes/`

## 원본 업데이트 받기

이 폴더는 GitHub에서 zip으로 받은 소스라 git 이력이 없음.
나중에 원본 업데이트를 합치려면 지금 상태로 git 저장소를 만들어 두는 게 좋음
(Android Studio: `VCS > Enable Version Control Integration > Git`, 그 다음 전체 커밋).
