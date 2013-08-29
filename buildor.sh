# Metabuild de apps android
IMAGES=../MobiPaperWeb/ElDia/images/

#declare -a apps=('eldia' 'pregon' 'ecosdiarios' 'castellanos');
#declare -a desc=('El Dia' 'Pregon' 'EcosDiarios' 'Castellanos');

declare -a apps=('pregon');
declare -a desc=('Pregon');

total=${#apps[*]}

for (( j=0; j<=$(( $total -1 )); j++ ))
do

  i=${apps[$j]}
  d=${desc[$j]}

  FOLDER=__buildor-$i
  rm -rf $FOLDER
  mkdir $FOLDER

  git archive master | tar -x -C $FOLDER  

  if [ $i == "eldia" ]; then continue; fi
   
  if [ ! -f $IMAGES/$i/bg.rgb ]; then
    echo "No puedo builder $i -> no tiene bg.rgb"
    exit -1
  fi

  for f in `find $FOLDER -name *.java | xargs grep -l com.diventi.eldia`; do
    perl -pi -e "s/com.diventi.eldia/com.diventi.$i/g" $f
  done

  mv $FOLDER/gen/com/diventi/eldia $FOLDER/gen/com/diventi/$i

  cp $IMAGES/$i/Icon@2x.png $FOLDER/res/drawable/icon.png
  cp $IMAGES/$i/logo.png $FOLDER/res/drawable/logo.png
  cp $IMAGES/$i/logo@2x.png $FOLDER/res/drawable/logo_solo.png

  BG=`head -c 7 $IMAGES/$i/bg.rgb`
  for f in `find $FOLDER/res -name "*.xml" | xargs grep -l "#3479c9"`; do
    perl -pi -e "s/#3479c9/$BG/g" $f
  done

  perl -pi -e "s/ElDia/$d/g" $FOLDER/build.xml
  
  perl -pi -e "s/El Dia/$d/g" $FOLDER/AndroidManifest.xml
  perl -pi -e "s/com.diventi.eldia/com.diventi.$i/g" $FOLDER/AndroidManifest.xml


done



