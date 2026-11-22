#!/usr/bin/env bash
#
# Runs locally what the GitHub Actions workflow (.github/workflows/quality-gate.yml) runs, except Sonar.
# If this script passes, the "Build, test and analyze" and "Enforce lint" steps of the workflow should pass too.
#
#   ./validate.sh            same steps as the workflow, from a clean build
#   ./validate.sh --quick    one strict pass (stops at the first failing tool); faster, for iterating
#   ./validate.sh --no-clean skip `clean`, reuse the existing target/ directory
#   ./validate.sh --help
#
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

quick=false
clean=clean
for arg in "$@"; do
    case "$arg" in
        --quick) quick=true ;;
        --no-clean) clean= ;;
        -h|--help) sed -n '2,10p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) echo "Unknown option: $arg (try --help)" >&2; exit 2 ;;
    esac
done

MVN=(./mvnw -B --no-transfer-progress)
log="$(mktemp "${TMPDIR:-/tmp}/validate.XXXXXX.log")"
trap 'rm -f "$log"' EXIT
started=$SECONDS

step() { printf '\n\033[1;34m==> %s\033[0m\n' "$1"; }
fail() { printf '\n\033[1;31mFAILED: %s\033[0m\n' "$1" >&2; exit 1; }

# the workflow runs on JDK 25
if ! java -version 2>&1 | grep -q '"25'; then
    fail "JDK 25 is required (the workflow uses it). Run 'sdk env' in this directory, then try again."
fi

if $quick; then
    step "Build, test and analyze, strict (clean=${clean:-no})"
    "${MVN[@]}" $clean verify -Plint -Dlint.fail=true 2>&1 | tee "$log" || fail "build, tests or analysis failed (see above)"
else
    # same as the workflow: lint in report-only mode first, so every tool runs and every report exists
    step "1/3 Build, test and analyze: verify -Plint (clean=${clean:-no})"
    "${MVN[@]}" $clean verify -Plint 2>&1 | tee "$log" || fail "build, tests or analysis failed (see above)"

    step "2/3 Check coverage report"
    test -s target/site/jacoco/jacoco.xml || fail "target/site/jacoco/jacoco.xml is missing"
    grep -o '<counter type="LINE"[^>]*/>' target/site/jacoco/jacoco.xml | tail -n 1

    step "3/3 Enforce lint: fails when Checkstyle, PMD or SpotBugs reported anything"
    "${MVN[@]}" verify -Plint -Dlint.fail=true -DskipTests || fail "lint findings (see above and the summary below)"
fi

# grep -c prints 0 but exits 1 when nothing matches, so its exit status must not trigger the fallback
count() {
    if [[ -f "$2" ]]; then
        grep -c "$1" "$2" || true
    else
        echo "n/a"
    fi
}

line_coverage() {
    local counter
    counter=$(grep -o '<counter type="LINE"[^>]*/>' target/site/jacoco/jacoco.xml | tail -n 1)
    local missed covered
    missed=$(sed -E 's/.*missed="([0-9]+)".*/\1/' <<<"$counter")
    covered=$(sed -E 's/.*covered="([0-9]+)".*/\1/' <<<"$counter")
    awk -v m="$missed" -v c="$covered" 'BEGIN { printf "%.1f%% (%d of %d lines)", 100 * c / (m + c), c, m + c }'
}

step "Summary"
# Surefire (unit) and Failsafe (integration) each print a total line: add them up
test_totals() {
    grep -E 'Tests run:.*[0-9]$' "$log" | sed -E 's/^\[[A-Z]+\] //' | awk -F'[:,]' '
        { run += $2; fail += $4; err += $6; skip += $8 }
        END { printf "Tests run: %d, Failures: %d, Errors: %d, Skipped: %d", run, fail, err, skip }'
}

printf '  %-28s %s\n' "Tests" "$(test_totals)"
printf '  %-28s %s\n' "Line coverage (JaCoCo)" "$(line_coverage)"
printf '  %-28s %s\n' "Error Prone warnings" "$(grep -cE '^\[WARNING\] [^ ]+\.java:\[' "$log" || true)"
printf '  %-28s %s\n' "Checkstyle violations" "$(count '<error ' target/checkstyle-result.xml)"
printf '  %-28s %s\n' "PMD violations" "$(count '<violation' target/pmd.xml)"
printf '  %-28s %s\n' "SpotBugs bugs" "$(count '<BugInstance' target/spotbugsXml.xml)"
printf '\n\033[1;32mAll checks passed\033[0m in %ds. Sonar runs on push.\n' $((SECONDS - started))
