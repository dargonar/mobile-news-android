var metayi;
var tmp_element;
function hasClass(el, name) {
  return new RegExp('(\\s|^)'+name+'(\\s|$)').test(el.className);
}
function addClass(el, name)
{
  if (!hasClass(el, name)) { el.className += (el.className ? ' ' : '') +name; }
}
function removeClass(el, name)
{
  if (hasClass(el, name)) {
    el.className=el.className.replace(new RegExp('(\\s|^)'+name+'(\\s|$)'),' ').replace(/^\s+|\s+$/g, '');
  }
}

function toggle(el_id, el_class){
  var el = document.getElementById(el_id);
  if(el==null)
    return false;
  if (!hasClass(el, el_class))
  {
    addClass(el, el_class);
    return false;
  }
  removeClass(el, el_class);
  return false;
}

function indexToLandscape(){
  var el = document.getElementById('index');
  removeClass(el, 'padded');
  addClass(el, 'padded_landscape');
}

function indexToPortrait(){
  var el = document.getElementById('index');
  removeClass(el, 'padded_landscape');
  addClass(el, 'padded');
}

function update_image(img) {
  var el = document.getElementById(img);
  if(el==null)
  {
    update_nota_abierta_image(img);
    return;
  }
  el.style.backgroundImage = '';
  el.style.backgroundImage = 'url(' + img + '.i)';
}

function onLoad(page_name) {
  console.error('ONLOADJS ' + page_name);
  update_all_images();
  jsinterface.onLoad();
}

function update_all_images() {
  refresh_background(document.getElementsByClassName('imagen'));
  refresh_background(document.getElementsByClassName('imagen_principal'));
  refresh_background(document.getElementsByClassName('imagen_secundaria'));
  refresh_background(document.getElementsByClassName('imagenNotaAbierta'));
}

function resize()
{
  console.error('Estoy dentro de RESIZE ' + document.body.getBoundingClientRect().width );
  jsinterface.onResize(document.body.getBoundingClientRect().width);
}

function refresh_background(imgs) {
  for (var i = 0; i < imgs.length; ++i) {
    var img = imgs[i];
    var url = img.style.backgroundImage;
    img.style.backgroundImage = '';
    img.style.backgroundImage = url;
  }
}

function update_imagen_nota_abierta(){
}

var timeout_var=null;
function show_actualizado(msg){
  clearTimeout(timeout_var);
  var el = document.getElementById('updated_msg');
  if(!el)
    return;
  el.innerHTML = msg;
  el.style.display = 'block';
  timeout_var=setTimeout(function(){el.style.display = 'none';},3000);
}