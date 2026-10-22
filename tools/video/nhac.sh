#!/bin/bash
# nhac.sh <ra.wav> <do dai s> <wav1> [wav2 ...] : noi (acrossfade 1.5 s), cat, mo vao 0.6 / ra 3.5, loudnorm 2 luot -14 LUFS TP -1.5
# Vi du: sh tools/video/nhac.sh nhac_2phut.wav 133.0 "Stage 1.wav" "Stage2.wav" "Stage 1.wav"  (do dai = "tong" cua dung_video.py)
RA=$1; L=$2; shift 2
IN=(); for f in "$@"; do IN+=(-i "$f"); done
n=$#; F=""; cur="[0:a]"
for ((i=1;i<n;i++)); do F="$F${cur}[$i:a]acrossfade=d=1.5[x$i];"; cur="[x$i]"; done
FO=$(python3 -c "print($L-3.5)")
F="$F${cur}atrim=0:$L,asetpts=N/SR/TB,afade=t=in:d=0.6,afade=t=out:st=$FO:d=3.5,aformat=sample_rates=48000:channel_layouts=stereo[a]"
T=$(mktemp --suffix=.wav)
ffmpeg -hide_banner -loglevel error -y "${IN[@]}" -filter_complex "$F" -map "[a]" -c:a pcm_s16le $T
J=$(ffmpeg -hide_banner -i $T -af loudnorm=I=-14:TP=-1.5:LRA=11:print_format=json -f null - 2>&1 | sed -n '/^{/,/^}/p')
g(){ echo "$J" | python3 -c "import json,sys;print(json.load(sys.stdin)['$1'])"; }
ffmpeg -hide_banner -loglevel error -y -i $T -af "loudnorm=I=-14:TP=-1.5:LRA=11:measured_I=$(g input_i):measured_TP=$(g input_tp):measured_LRA=$(g input_lra):measured_thresh=$(g input_thresh):offset=$(g target_offset):linear=true,aresample=48000" -c:a pcm_s16le "$RA"
rm -f $T
