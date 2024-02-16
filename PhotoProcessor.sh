#!/bin/bash

if [ $# -lt 1 ]; then
    echo "Usage: $0 [options] <directory>"
    exit 1
fi

RESIZE="off"
QUALITY="default"
THUMBNAILSIZE="off"
WTEXT=""
WFONT="Liberation-Sans"
WFONTSIZE="auto"
WMARGIN="auto"
WROT=0
WDISTORT="off"
WOPACITY="30"
WCOLOR="black"
REMOVEORIGINAL="no"

while [[ $# -gt 1 ]]; do
    case "$1" in
        -resize)
            shift
            RESIZE="$1"
            shift
            ;;
        -quality)
            shift
            QUALITY="$1"
            shift
            ;;
        -removeoriginal)
            REMOVEORIGINAL="yes"
            shift
            ;;
        -thumbnail)
            shift
            THUMBNAILSIZE="$1"
            shift
            ;;
        -wtext)
            shift
            WTEXT="$1"
            shift
            ;;
        -wfont)
            shift
            WFONT="$1"
            shift
            ;;
        -wfontsize)
            shift
            WFONTSIZE="$1"
            shift
            ;;
        -wmargin)
            shift
            WMARGIN="$1"
            shift
            ;;
        -wrotation)
            shift
            WROT="$1"
            shift
            ;;
        -walpha)
            shift
            WOPACITY="$1"
            shift
            ;;
        -wcolor)
            shift
            WCOLOR="$1"
            shift
            ;;
        -wdistortion)
            shift
            WDISTORT="$1"
            shift
            ;;
       *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

DIRECTORY=$1
if [ ! -d "$DIRECTORY" ]
  then
    echo "Directory does not exist."
    exit 1
fi

echo "Starting conversion in ${DIRECTORY}"
for f in ${DIRECTORY}/*
do
    if [[ ! $f == *"_thumb.jpg" ]] # Ignore thumbnails
    then
        filename=$(basename -- "$f")
        extension="${filename##*.}"
        name="${filename%.*}"
        width=$(convert $f -print "%w" /dev/null)
        height=$(convert $f -print "%h" /dev/null)
        SIZE="${width}x${height}"
        if [[ ! $name == *"_${SIZE}" ]] # Check if already processed
        then
            echo "<< Processing: ${name}.${extension} (Size: ${width}x${height})"
            if [[ ! $RESIZE == "off" ]]
            then
                SIZE=${RESIZE}
            fi
            tmpf="${DIRECTORY}/${name}_${SIZE}_tmp.jpg"
            echo "Converting (Format: JPEG, Shrink to fit: $SIZE, Quality: ${QUALITY})"
            if [[ $QUALITY == "default" ]]
            then
                if [[ $RESIZE == "off" ]]
                then
                    convert "$f" "$tmpf"
                else
                    convert "$f" -resize ${SIZE}\> "$tmpf"
                fi
            else
                if [[ $RESIZE == "off" ]]
                then
                    convert "$f" -quality ${QUALITY}% "$tmpf"
                else
                    convert "$f" -resize ${SIZE}\> -quality ${QUALITY}% "$tmpf"
                fi
            fi
            width=$(convert $tmpf -print "%w" /dev/null)
            height=$(convert $tmpf -print "%h" /dev/null)
            SIZE="${width}x${height}"
            newname="${name}_${SIZE}"
            newf="${DIRECTORY}/${newname}.jpg"
            thumbf="${DIRECTORY}/${newname}_thumb.jpg"
            waterf="${DIRECTORY}/${newname}_water.png"
            overlayf="${DIRECTORY}/${newname}_overlay.png"
            FILESIZE=$(du -h ${tmpf} | awk '{ print $1 }')
            mv "$tmpf" "$newf"
            echo "Created ${newname}.jpg (Size: ${width}x${height}, File Size: ${FILESIZE})"
            if [[ ! ${WTEXT} == "" ]]
            then
                if [[ $WFONTSIZE == "auto" ]]
                then
                    WWFONTSIZE=$( echo "${height} / 20" | bc )
                    if [ "$WWFONTSIZE" -lt "12" ]
                    then
                        WWFONTSIZE=12
                    fi
                else
                    WWFONTSIZE=$WFONTSIZE
                fi
                if [[ $WMARGIN == "auto" ]]
                then
                    WWMARGIN=$( echo "1.5 * $WWFONTSIZE" | bc )
                else
                    WWMARGIN=$WMARGIN
                fi
                echo "Preparing watermark (Text: ${WTEXT}, Font: ${WFONT}, Font Size: ${WWFONTSIZE}, Margin: ${WWMARGIN}, Rotation: ${WROT}, Color: ${WCOLOR})"
                if [[ $WDISTORT == "off" ]]
                then
                    WROLW=0
                    WROLH=0
                    WWAVELENGTH=0
                    WWAVEHEIGHT=0
                    WSWIRL=0
                    OVERLAYHEIGHT=${height}
                    OVERLAYWIDTH=$width
                else
                    if [[ $WDISTORT == "auto" ]]
                    then
                        WFACTOR=50
                    else
                        WFACTOR=$WDISTORT
                    fi
                    WROLW=$( echo "1 + ${RANDOM} % ${width}" | bc )
                    WROLH=$( echo "1 + ${RANDOM} % ${height}" | bc )
                    WWAVELENGTH=$width
                    WROT=$( echo "${WROT} + ${RANDOM} % ( ( ${WFACTOR} * 30 ) / 100 )" | bc )
                    WWAVEHEIGHT=$( echo "1 + ${RANDOM} % ( ( ${WFACTOR} * ${height} ) / 500 )" | bc )
                    WSWIRL=$( echo "1 + ${RANDOM} % ( ( ${WFACTOR} * 30 ) / 100 )" | bc )
                    echo "Distorting (Roll: ${WROLW}x${WROLH}, Rotation: ${WROT}, Wave Amplitude: ${WWAVEHEIGHT}, Wave Length: ${WWAVELENGTH}, Swirl: ${WSWIRL})"
                    OVERLAYHEIGHT=$( echo "${height} + 4 * ${WWAVEHEIGHT}" | bc )
                    OVERLAYWIDTH=$width
                fi
                convert -background transparent -fill ${WCOLOR} -pointsize ${WWFONTSIZE} -font "${WFONT}" -gravity center label:"${WTEXT}" -rotate -${WROT} -trim -bordercolor transparent -border ${WWMARGIN} "${waterf}"
                convert -size ${width}x${OVERLAYHEIGHT} xc: -tile-offset -${WROLW}-${WROLH} +size -tile "${waterf}"  -draw "color 0,0 reset" -background transparent -wave ${WWAVEHEIGHT}x${WWAVELENGTH} -gravity center -crop ${SIZE}+0+0 +repage "${overlayf}"
                echo "Applying watermark to ${newname}.jpg (Alpha: ${WOPACITY})"
                convert "${newf}" \( "${overlayf}" -swirl ${WSWIRL} \) -compose dissolve -define compose:args="${WOPACITY},100" -composite "${tmpf}"
                mv "$tmpf" "$newf"
                rm "$waterf" "$overlayf"
            fi
            if [[ ! ${THUMBNAILSIZE} == "off" ]]
            then
                echo "Creating thumbnail (${THUMBNAILSIZE}): ${newname}_thumb.jpg"
                convert -thumbnail ${THUMBNAILSIZE} -unsharp 0x.5 "$f" "$thumbf"
            fi
            if [[ $REMOVEORIGINAL == "yes" ]]
            then
                rm "$f"
            fi
            echo ">> Finished: ${name}.${extension}"
            fi
    fi
done
echo "Conversion finished!"
