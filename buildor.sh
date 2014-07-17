# Metabuild de apps android
IMAGES=../MobiPaperiOS/ElDia/images/

#declare -a apps=('eldia'  'pregon' 'ecosdiarios' 'castellanos' 'lareforma'  'elnorte');
#declare -a desc=('El Dia' 'Pregon' 'EcosDiarios' 'Castellanos' 'La Reforma' 'El Norte');

declare -a apps=('puertonegocios');
declare -a desc=('Puerto Negocios');

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

    menem=$i"2"
    for f in `find $FOLDER -name *.java | xargs grep -l com.diventi.eldia`; do
      perl -pi -e "s/com.diventi.eldia/com.diventi.$menem/g" $f
    done

    mv $FOLDER/gen/com/diventi/eldia $FOLDER/gen/com/diventi/$menem

    ii=$i
    if [ $i == "castellanos" ]; then
      ii=castellanos_new
    fi

    cp $IMAGES/$ii/logo.png $FOLDER/res/drawable/logo.png
    cp $IMAGES/$ii/logo@2x.png $FOLDER/res/drawable/logo_solo.png

    cp $IMAGES/$ii/android/icon.png $FOLDER/res/drawable
    cp $IMAGES/$ii/android/back.png $FOLDER/res/drawable
    cp $IMAGES/$ii/android/list.png $FOLDER/res/drawable
    cp $IMAGES/$ii/android/refresh.png $FOLDER/res/drawable
    cp $IMAGES/$ii/android/refresh.png $FOLDER/res/drawable
    cp $IMAGES/$ii/android/share.png $FOLDER/res/drawable
    cp $IMAGES/$ii/android/warning.48x48.png $FOLDER/res/drawable

    BG=`head -c 7 $IMAGES/$ii/bg.rgb`
    for f in `find $FOLDER/res -name "*.xml" | xargs grep -l "#3479c9"`; do
      perl -pi -e "s/#3479c9/$BG/g" $f
    done

    perl -pi -e "s/ElDia/$d/g" $FOLDER/build.xml
    
    perl -pi -e "s/El Dia/$d/g" $FOLDER/AndroidManifest.xml
    perl -pi -e "s/com.diventi.eldia/com.diventi.$menem/g" $FOLDER/AndroidManifest.xml

  fi

  cd $FOLDER
  ant $1

  if [ $? -eq 0 ]; then
    cp "bin/$d-$1.apk" ../apks-to-upload
  fi

  cd ..

done



