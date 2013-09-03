# Metabuild de apps android
IMAGES=../MobiPaperWeb/ElDia/images/

#declare -a apps=('eldia' 'pregon' 'ecosdiarios' 'castellanos');
#declare -a desc=('El Dia' 'Pregon' 'EcosDiarios' 'Castellanos');

declare -a apps=('eldia'  'pregon' 'ecosdiarios' 'castellanos');
declare -a desc=('El Dia' 'Pregon' 'EcosDiarios' 'Castellanos');

#declare -a apps=('pregon');
#declare -a desc=('Pregon');

total=${#apps[*]}

rm -rf apks-to-upload
mkdir apks-to-upload

for (( j=0; j<=$(( $total -1 )); j++ ))
do

  i=${apps[$j]}
  d=${desc[$j]}

  FOLDER=__buildor-$i
  rm -rf $FOLDER
  mkdir $FOLDER

  git archive master | tar -x -C $FOLDER  

  if [ $i != "eldia" ]; then 
   
    if [ ! -f $IMAGES/$i/bg.rgb ]; then
      echo "No puedo builder $i -> no tiene bg.rgb"
      exit -1
    fi

    for f in `find $FOLDER -name *.java | xargs grep -l com.diventi.eldia`; do
      perl -pi -e "s/com.diventi.eldia/com.diventi.$i/g" $f
    done

    mv $FOLDER/gen/com/diventi/eldia $FOLDER/gen/com/diventi/$i

    cp $IMAGES/$i/logo.png $FOLDER/res/drawable/logo.png
    cp $IMAGES/$i/logo@2x.png $FOLDER/res/drawable/logo_solo.png

    cp $IMAGES/$i/android/icon.png $FOLDER/res/drawable
    cp $IMAGES/$i/android/back.png $FOLDER/res/drawable
    cp $IMAGES/$i/android/list.png $FOLDER/res/drawable
    cp $IMAGES/$i/android/refresh.png $FOLDER/res/drawable
    cp $IMAGES/$i/android/refresh.png $FOLDER/res/drawable
    cp $IMAGES/$i/android/share.png $FOLDER/res/drawable
    cp $IMAGES/$i/android/warning.48x48.png $FOLDER/res/drawable

    BG=`head -c 7 $IMAGES/$i/bg.rgb`
    for f in `find $FOLDER/res -name "*.xml" | xargs grep -l "#3479c9"`; do
      perl -pi -e "s/#3479c9/$BG/g" $f
    done

    perl -pi -e "s/ElDia/$d/g" $FOLDER/build.xml
    
    perl -pi -e "s/El Dia/$d/g" $FOLDER/AndroidManifest.xml
    perl -pi -e "s/com.diventi.eldia/com.diventi.$i/g" $FOLDER/AndroidManifest.xml

  fi

  cd $FOLDER
  ant $1

  if [ $? -eq 0 ]; then
    NAME=`echo "$d" | sed 's/ //g'`
    cp bin/$NAME-$1.apk ../apks-to-upload
  fi

  cd ..

done



