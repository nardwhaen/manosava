# Manosaba Pixel Dungeon — Claude 작업 안내

Shattered Pixel Dungeon v4.0.0 기반 모드 (GPLv3). 자세한 빌드·구조 메모는 `MOD_README.md`.

## 꼭 지킬 것
- 사용자에게는 **항상 한국어**로 답한다.
- 파일을 덮어쓰기 전에 `_manosaba_backup/before_<작업이름>/`에 원본을 백업한다.
- 지우거나 되돌리기 어려운 변경, 애매한 변경은 먼저 물어본다.
- 원본 직업은 지우지 않고 선택 화면에서만 숨긴다 (`HeroClass.isSelectable()`).
- 서명 키(`keystore.properties`, `*.keystore`, `*.jks`)와 `.env`는 공개 저장소(GitHub nardwhaen/manosava)에 올리지 않는다. API 키는 화면에 출력하거나 다른 파일에 적지 않는다.
- 공식 일러스트를 그대로 변환하지 않는다. 사용자가 준 이미지를 넣거나 새로 픽셀 아트를 그리는 것은 괜찮다.
- 모드에서 바꾼 코드에는 `//[Manosaba]` 주석을 단다.

## 영웅 자리
| 내부 이름 | 캐릭터 | 도우미 코드 |
|---|---|---|
| WARRIOR | 타치바나 셰리 | `actors/hero/sherry/` |
| MAGE | 죠가사키 노아 | `actors/hero/noa/`, `actors/hero/abilities/noa/`, `items/artifacts/PaintCan.java` |

기획안·텍스트 초안은 노션 "Manosaba Pixel Dungeon 기획" 페이지에 있다.

## 영웅 스프라이트 규칙 (중요)
- 모든 캐릭터는 **셰리와 같은 틀**(눈·얼굴·몸 형태)을 쓴다. 틀: `_manosaba_art/template/hero_template.png` (aseprite: `hero_template.aseprite`)
- 시트 크기 256x32, 칸 크기 **12x17**, 한 줄에 21칸 (x = 칸번호*12, y = 0).
  - 0-1 서기, 2-7 달리기, 8-12 쓰러짐, 13-15 공격, 16-17 조작, 18 비행, 19-20 읽기(책)
- 틀 위에 머리카락·옷·모자만 얹고 눈 색만 바꾼다. 눈·얼굴·몸 윤곽은 바꾸지 않는다.
- 예시: 셰리 `core/src/main/assets/sprites/sherry.png`, 노아 견본 `core/src/main/assets/sprites/noa.png` (`_manosaba_art/noa.aseprite`)
- 게임은 `HeroSprite.singleRow()`에 들어 있는 직업만 이 한 줄짜리 시트로 읽는다.

## 이미지 생성 (OpenAI API)
- 키와 모델은 프로젝트 폴더의 `.env`: `OPENAI_API_KEY`, `OPENAI_IMAGE_MODEL` (gpt-image-2.5-sunburst)
- 생성 이미지는 **참고용 콘셉트**로 쓰고, 게임 스프라이트는 반드시 위 틀(12x17 칸)에 맞춰 픽셀 단위로 정리한다. 생성 이미지를 그대로 축소해서 시트에 넣지 않는다.
- 결과물은 `_manosaba_art/`에 저장한다.

## 컴파일 확인
`gradle desktop:debug` (Android Studio Gradle 탭 → desktop > Tasks > other > debug). 디버그 빌드에서는 모든 직업이 해금된다.
테스트 시드 `SHERRYTEST`: 가면·왕관·강화 주문서 등 지급.
