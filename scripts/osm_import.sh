#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$ROOT/.firecrawl/osm"
mkdir -p "$OUT"

# Step 1: Nominatim lookup (parallel, 2 at a time)
lines=()
while IFS= read -r line; do
  lines+=("$line")
done < <(tail -n +2 "$ROOT/scripts/osm_campuses.csv")

run_pair() {
  local slug1="$1" url1="$2" slug2="$3" url2="$4"
  if [[ -n "$slug1" ]]; then
    firecrawl scrape "$url1" --only-main-content -o "$OUT/nominatim-${slug1}.md" &
  fi
  if [[ -n "$slug2" ]]; then
    firecrawl scrape "$url2" --only-main-content -o "$OUT/nominatim-${slug2}.md" &
  fi
  wait
}

i=0
while [[ $i -lt ${#lines[@]} ]]; do
  IFS=',' read -r slug name query radius <<< "${lines[$i]}"
  url1="https://nominatim.openstreetmap.org/search?q=$(python3 - <<PY
import urllib.parse
print(urllib.parse.quote("$query"))
PY
)&format=json&limit=1"
  slug1="$slug"
  url2=""
  slug2=""
  j=$((i+1))
  if [[ $j -lt ${#lines[@]} ]]; then
    IFS=',' read -r slug name query radius <<< "${lines[$j]}"
    url2="https://nominatim.openstreetmap.org/search?q=$(python3 - <<PY
import urllib.parse
print(urllib.parse.quote("$query"))
PY
)&format=json&limit=1"
    slug2="$slug"
  fi
  run_pair "$slug1" "$url1" "$slug2" "$url2"
  i=$((i+2))
  sleep 1
 done

# Step 2: Generate Overpass URLs
python3 "$ROOT/scripts/osm_pipeline.py" stage1

# Step 3: Overpass fetch (parallel, 2 at a time)
urls=()
while IFS= read -r line; do
  urls+=("$line")
done < <(ls "$OUT"/overpass-*.url)

i=0
while [[ $i -lt ${#urls[@]} ]]; do
  urlfile1="${urls[$i]}"
  url1="$(cat "$urlfile1")"
  slug1="$(basename "$urlfile1" | sed 's/overpass-//; s/\.url//')"
  urlfile2=""
  url2=""
  slug2=""
  j=$((i+1))
  if [[ $j -lt ${#urls[@]} ]]; then
    urlfile2="${urls[$j]}"
    url2="$(cat "$urlfile2")"
    slug2="$(basename "$urlfile2" | sed 's/overpass-//; s/\.url//')"
  fi
  if [[ -n "$slug1" ]]; then
    firecrawl scrape "$url1" --only-main-content -o "$OUT/overpass-${slug1}.md" &
  fi
  if [[ -n "$slug2" ]]; then
    firecrawl scrape "$url2" --only-main-content -o "$OUT/overpass-${slug2}.md" &
  fi
  wait
  i=$((i+2))
  sleep 1
 done

# Step 4: Build CSV + SQL
python3 "$ROOT/scripts/osm_pipeline.py" stage2

printf "\nOSM import completed. Data written to data/osm_buildings.csv and src/main/resources/osm_buildings.sql\n"
