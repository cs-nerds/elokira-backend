# shellcheck disable=SC2002
# shellcheck disable=SC2046
export $(cat local.env | grep -v '#' | awk '/=/ {print $1}')