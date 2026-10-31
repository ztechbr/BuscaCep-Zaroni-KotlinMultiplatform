#!/usr/bin/env bash
#
# Compila e executa o BuscaCEP no Android (emulador), no iOS (simulador) e no Desktop.
#
# Uso:
#   ./runall.sh                  executa as três versões
#   ./runall.sh android ios      executa apenas as versões indicadas
#
# Variáveis opcionais:
#   ANDROID_AVD     AVD a iniciar quando não há emulador ativo (padrão: Medium_Phone)
#   IOS_SIMULATOR   UDID do simulador (padrão: iPhone já iniciado ou o primeiro disponível)
#
# O Desktop roda por último e em primeiro plano: o script termina quando a janela é fechada.

set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$RAIZ"

ANDROID_AVD="${ANDROID_AVD:-Medium_Phone}"
PACOTE_ANDROID="io.ztechnologies.buscacep"
DERIVED_DATA="$RAIZ/build/ios-derived-data"

etapa() { printf '\n==> %s\n' "$1"; }
falha() { printf 'Erro: %s\n' "$1" >&2; exit 1; }

local_sdk() {
    if [ -n "${ANDROID_HOME:-}" ]; then
        echo "$ANDROID_HOME"
    elif [ -f local.properties ] && grep -q '^sdk.dir=' local.properties; then
        sed -n 's/^sdk\.dir=//p' local.properties
    else
        echo "$HOME/Library/Android/sdk"
    fi
}

executar_android() {
    local sdk adb serial
    sdk="$(local_sdk)"
    adb="$sdk/platform-tools/adb"
    [ -x "$adb" ] || falha "adb não encontrado em $adb"

    etapa "Android: preparando o emulador"
    serial="$("$adb" devices | awk '/^emulator-.*\tdevice$/ { print $1; exit }')"
    if [ -z "$serial" ]; then
        mkdir -p build
        nohup "$sdk/emulator/emulator" -avd "$ANDROID_AVD" > build/emulator.log 2>&1 &
        disown
        "$adb" wait-for-device
        serial="$("$adb" devices | awk '/^emulator-.*\tdevice$/ { print $1; exit }')"
    fi
    [ -n "$serial" ] || falha "nenhum emulador disponível; veja build/emulator.log"

    local tentativas=0
    until [ "$("$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
        tentativas=$((tentativas + 1))
        [ "$tentativas" -le 150 ] || falha "o emulador não terminou de iniciar em 5 minutos"
        sleep 2
    done

    etapa "Android: compilando e instalando em $serial"
    ANDROID_SERIAL="$serial" sh ./gradlew :androidApp:installDebug --console=plain

    etapa "Android: abrindo o aplicativo"
    "$adb" -s "$serial" shell am start -n "$PACOTE_ANDROID/.MainActivity"
}

escolher_simulador() {
    if [ -n "${IOS_SIMULATOR:-}" ]; then
        echo "$IOS_SIMULATOR"
        return
    fi
    local udid
    udid="$(xcrun simctl list devices booted | awk '/iPhone/ { print; exit }' | grep -oE '[0-9A-F-]{36}' || true)"
    if [ -z "$udid" ]; then
        udid="$(xcrun simctl list devices available | awk '/iPhone/ { print; exit }' | grep -oE '[0-9A-F-]{36}' || true)"
    fi
    echo "$udid"
}

executar_ios() {
    command -v xcodebuild > /dev/null || falha "xcodebuild não encontrado; o iOS exige macOS com Xcode"

    local udid app bundle_id
    udid="$(escolher_simulador)"
    [ -n "$udid" ] || falha "nenhum simulador de iPhone disponível"

    etapa "iOS: iniciando o simulador $udid"
    xcrun simctl bootstatus "$udid" -b > /dev/null
    # O simulador funciona sem janela; o Xcode 27 pode não trazer o Simulator.app.
    open -a Simulator 2> /dev/null || echo "Aviso: Simulator.app não encontrado; o aplicativo rodará sem janela."

    etapa "iOS: compilando"
    xcodebuild \
        -project iosApp/iosApp.xcodeproj \
        -scheme iosApp \
        -configuration Debug \
        -sdk iphonesimulator \
        -destination "id=$udid" \
        -derivedDataPath "$DERIVED_DATA" \
        -quiet \
        build

    app="$DERIVED_DATA/Build/Products/Debug-iphonesimulator/BuscaCEP.app"
    bundle_id="$(/usr/libexec/PlistBuddy -c 'Print CFBundleIdentifier' "$app/Info.plist")"

    etapa "iOS: instalando e abrindo $bundle_id"
    xcrun simctl install "$udid" "$app"
    xcrun simctl launch --terminate-running-process "$udid" "$bundle_id"
}

executar_desktop() {
    etapa "Desktop: compilando e abrindo (feche a janela para encerrar)"
    sh ./gradlew :desktopApp:run --console=plain
}

if [ "$#" -eq 0 ]; then
    set -- android ios desktop
fi

for alvo in "$@"; do
    case "$alvo" in
        android | ios | desktop) ;;
        *) falha "alvo desconhecido: $alvo (use android, ios ou desktop)" ;;
    esac
done

# O Desktop bloqueia o terminal, então fica sempre por último.
for alvo in android ios; do
    case " $* " in *" $alvo "*) "executar_$alvo" ;; esac
done
case " $* " in *" desktop "*) executar_desktop ;; esac
