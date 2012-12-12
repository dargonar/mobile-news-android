function update_image(img) {
  var el = document.getElementById(img);
  if(el==null) {
    update_nota_abierta_image(img);
    return;
  }

  el.style.backgroundImage = '';
  el.style.backgroundImage = 'url(' + img + '.i)';
}

function update_all_images() {
  refresh_background(document.getElementsByClassName('imagen_principal'));
  refresh_background(document.getElementsByClassName('imagen_secundaria'));
}

function refresh_background(imgs) {
  for (var i = 0; i < imgs.length; ++i) {
    var img = imgs[i];
    var url = img.style.backgroundImage;
    img.style.backgroundImage = '';
    img.style.backgroundImage = url;
  }
}

function update_nota_abierta_image(img){
  var el = document.getElementById('img_'+img);
  
  if(el == null)
    return;

  el.src = '';
  el.src = 'url(' + img + '.i)';
}

var timeout_var=null;
function show_actualizado(msg){
  clearTimeout(timeout_var);
  var el = document.getElementById('updated_msg');
  
  //console.error('show_actualizado: ' + (el == null) );
  
  if(!el)
    return;
  
  //console.error('show_actualizado: ' + (el == null) );
  
  el.innerHTML = msg;
  el.style.display = 'block';
  timeout_var=setTimeout(function(){el.style.display = 'none';},3000);
}

function meta_head_trick() {
  
  var scale = document.width/320;

  //console.error('or:' + window.orientation + ',w:' + screen.width + ',h:' + screen.height + ',s:' + scale);

  //if( screen.width > 700 )
  //document.head.insertAdjacentHTML( 'beforeEnd', '<meta name="viewport" content="initial-scale=2.4, maximum-scale=2.4, user-scalable=no, width=320px" />' );
  //else
  //scale=2.0;
  //var meta_str = '<meta name="viewport" content="target-densitydpi=target-dpi, initial-scale='+scale+', maximum-scale='+scale+', user-scalable=no, width=320" />';
  //scale = 2.5;
  var meta_str = '<meta name="viewport" content="initial-scale='+scale+', user-scalable=no, width=320" />';
  //console.error(meta_str);
  
  document.head.insertAdjacentHTML( 'beforeEnd', meta_str );
}

function text_size(size1, size2) {
  document.getElementById('informacion').style.fontSize = size1+'em';
  document.getElementById('bajada').style.fontSize= size2+'em';
}

  
  
  
