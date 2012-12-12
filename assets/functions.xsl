<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:media="http://search.yahoo.com/mrss/"
  xmlns:news="http://www.diariosmoviles.com.ar/news-rss/">
  
  <xsl:variable name="ascii"> !"#$%&amp;'()*+,-./0123456789:;&lt;=&gt;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
  <xsl:variable name="latin1">&#160;&#161;&#162;&#163;&#164;&#165;&#166;&#167;&#168;&#169;&#170;&#171;&#172;&#173;&#174;&#175;&#176;&#177;&#178;&#179;&#180;&#181;&#182;&#183;&#184;&#185;&#186;&#187;&#188;&#189;&#190;&#191;&#192;&#193;&#194;&#195;&#196;&#197;&#198;&#199;&#200;&#201;&#202;&#203;&#204;&#205;&#206;&#207;&#208;&#209;&#210;&#211;&#212;&#213;&#214;&#215;&#216;&#217;&#218;&#219;&#220;&#221;&#222;&#223;&#224;&#225;&#226;&#227;&#228;&#229;&#230;&#231;&#232;&#233;&#234;&#235;&#236;&#237;&#238;&#239;&#240;&#241;&#242;&#243;&#244;&#245;&#246;&#247;&#248;&#249;&#250;&#251;&#252;&#253;&#254;&#255;</xsl:variable>
  <xsl:variable name="safe">!'()*-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>
  <xsl:variable name="hex" >0123456789ABCDEF</xsl:variable>
  
    <xsl:template name="url-encode">
    <xsl:param name="str"/>   
    <xsl:if test="$str">
      <xsl:variable name="first-char" select="substring($str,1,1)"/>
      <xsl:choose>
        <xsl:when test="contains($safe,$first-char)">
          <xsl:value-of select="$first-char"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="codepoint">
            <xsl:choose>
              <xsl:when test="contains($ascii,$first-char)">
                <xsl:value-of select="string-length(substring-before($ascii,$first-char)) + 32"/>
              </xsl:when>
              <xsl:when test="contains($latin1,$first-char)">
                <xsl:value-of select="string-length(substring-before($latin1,$first-char)) + 160"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:message terminate="no">Warning: string contains a character that is out of range! Substituting "?".</xsl:message>
                <xsl:text>63</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
        <xsl:variable name="hex-digit1" select="substring($hex,floor($codepoint div 16) + 1,1)"/>
        <xsl:variable name="hex-digit2" select="substring($hex,$codepoint mod 16 + 1,1)"/>
        <xsl:value-of select="concat('%',$hex-digit1,$hex-digit2)"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="string-length($str) &gt; 1">
        <xsl:call-template name="url-encode">
          <xsl:with-param name="str" select="substring($str,2)"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  
  <!--  Con estas variables podemos convertir un string en upper case o lower case. 
        -) translate($variable, $smallcase, $uppercase)
        No se utilizan
  -->  
  <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
  
  <!-- Formateo de fecha en HH:mm -->
  <xsl:template name="FormatDate">
    <xsl:param name="DateTime" />
    <xsl:variable name="mo">
      <xsl:value-of select="substring($DateTime,1,3)" />
    </xsl:variable>
    <xsl:variable name="day-temp">
      <xsl:value-of select="substring-after($DateTime,', ')" />
    </xsl:variable>
    <xsl:variable name="day">
      <xsl:value-of select="substring-before($day-temp,' ')" />
    </xsl:variable>
    <xsl:variable name="month-year-temp">
      <xsl:value-of select="substring-after($day-temp,' ')" />
    </xsl:variable>
    <xsl:variable name="month">
      <xsl:value-of select="substring-before($month-year-temp,' ')" />
    </xsl:variable>
    <xsl:variable name="year-time-temp">
      <xsl:value-of select="substring-after($month-year-temp,' ')" />
    </xsl:variable>
    <xsl:variable name="year">
      <xsl:value-of select="substring-before($year-time-temp,' ')" />
    </xsl:variable>
    <xsl:variable name="time">
      <xsl:value-of select="substring-after($year-time-temp,' ')" />
    </xsl:variable>
    <xsl:variable name="hh">
      <xsl:value-of select="substring-before($time,':')" />
    </xsl:variable>
    <xsl:variable name="mm_ss">
      <xsl:value-of select="substring-after($time,':')" />
    </xsl:variable>
    <xsl:variable name="mm">
      <xsl:value-of select="substring-before($mm_ss,':')" />
    </xsl:variable>
    <xsl:variable name="ss_gmt">
      <xsl:value-of select="substring-after($mm_ss,':')" />
    </xsl:variable>
    <xsl:variable name="ss">
      <xsl:value-of select="substring-before($ss_gmt,' ')" />
    </xsl:variable>
    <xsl:value-of select="$hh"/>
    <xsl:value-of select="':'"/>
    <xsl:value-of select="$mm"/>
  </xsl:template>
  
  <!-- Es el template de la noticia destacada en listado principal de noticias.
        Recibe al nodo "item"(Node) como parametro. -->
  <xsl:template name="DestacadaEnListadoPrincipal">
    <xsl:param name="Node" />
    <div id="nota">
      <xsl:variable name="encoded_url" >
        <xsl:call-template name="url-encode">
          <xsl:with-param name="str" select="$Node/link"/>
        </xsl:call-template>
      </xsl:variable>
      <a href="noticia://{$Node/guid}?url={$encoded_url}&amp;title={$Node/title}&amp;header={$Node/description}" title="principal">
        <xsl:if test="not(not($Node/thumbnail))" >
          <xsl:call-template name="ImagenNoticiaDestacada">
            <xsl:with-param name="ImageUrl" select="$Node/thumbnail/@url"/>
            <xsl:with-param name="MetaTag" select="$Node/meta"/>
          </xsl:call-template>
        </xsl:if>
        <div class="contenido">
          <div id="titulo">
            <label>
              <xsl:call-template name="FormatDate">
                <xsl:with-param name="DateTime" select="$Node/pubDate"/>
              </xsl:call-template>
            </label> | <label class="seccion"><xsl:value-of disable-output-escaping="yes" select="$Node/category" /></label>
            <br />
            <h1><xsl:value-of disable-output-escaping="yes" select="$Node/title" /></h1>
          </div>
        </div>
      </a>
      <div class="separador"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
    </div>
  </xsl:template>
  
  <!-- Template de la imagen grande de la noticia destacada en el listado principal (DestacadaEnListadoPrincipal). -->
  <xsl:template name="ImagenNoticiaDestacada">
    <xsl:param name="ImageUrl" />
    <xsl:param name="MetaTag" />
    <div class="main_img_container">
      <div class="imagen_principal" id="{$ImageUrl}" style="background-image:url({$ImageUrl}.i);"></div>
      <div class="media_link video_over_photo">
        <xsl:call-template name="MediaAttach">
          <xsl:with-param name="MetaTag" select="$MetaTag"/>
        </xsl:call-template>
      </div>
    </div>
  </xsl:template>
  
  <!-- Template del listado de noticias uniforme. Para listado principal o de seccion. -->
  <xsl:template name="ListadoNoticiasEnListado">
    <xsl:param name="Nodes" />
    <div id="listado" style="display:block;">
      <ul class="main_list">
        <xsl:for-each select="$Nodes">
          <xsl:call-template name="NoticiaEnListado">
            <xsl:with-param name="Node" select="."/>
          </xsl:call-template>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:template>
  
  <!-- Template de la noticia en listado de noticias uniforme (ListadoNoticiasEnListado). Para listado principal o de seccion. -->  
  <xsl:template name="NoticiaEnListado">
    <xsl:param name="Node" />
    
    <xsl:variable name="has_image" select="not(not($Node/thumbnail))"></xsl:variable>
    <xsl:variable name="full_width" >
      <xsl:if test="not($has_image)">
        <xsl:text>full_width</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="encoded_url" >
      <xsl:call-template name="url-encode">
        <xsl:with-param name="str" select="$Node/link"/>
      </xsl:call-template>
    </xsl:variable>
    <li>
      <a href="noticia://{$Node/guid}?url={$encoded_url}&amp;title={$Node/title}&amp;header={$Node/description}" title="">
        <div class="titular {$full_width}">
          <label>
            <xsl:call-template name="FormatDate">
              <xsl:with-param name="DateTime" select="$Node/pubDate"/>
            </xsl:call-template>
          </label><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>|<xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
          <label class="seccion">
            <xsl:call-template name="ReplaceInfoGral">
              <xsl:with-param name="seccion" select="$Node/category"/>
            </xsl:call-template>
          </label><br />
          <label class="titulo"><xsl:value-of disable-output-escaping="yes" select="$Node/title" /></label>
        </div>
        
        <xsl:if test="not(not($has_image))">
          <div class="foto img_container">
            <xsl:if test="not(not($Node/meta))">
              <xsl:call-template name="MediaAttach">
                <xsl:with-param name="MetaTag" select="$Node/meta"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="not(not($Node/thumbnail))">
              <div class="imagen_secundaria" id="{$Node/thumbnail/@url}" style="background-image:url({$Node/thumbnail/@url}.i) !important;"></div>
              <div class="img_loader"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
              <!-- img src="{$Node/thumbnail/@url}" / -->
            </xsl:if>
            <xsl:if test="not($Node/thumbnail)">
              <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
            </xsl:if>
          </div>
        </xsl:if>
        <xsl:if test="not($has_image)">
          <xsl:if test="not(not($Node/meta))">
            <div class="right_ico_container">
              <xsl:call-template name="MediaAttach">
                <xsl:with-param name="MetaTag" select="$Node/meta"/>
              </xsl:call-template>
            </div>
          </xsl:if>
        </xsl:if>
      </a>
      <div class="separador"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
    </li>
  </xsl:template>
  
  <!-- Template para indicar que elementos multimedia que tiene la noticia. -->
  <xsl:template name="MediaAttach">
    <!-- <meta has_gallery="true" has_video="false" has_audio="false" /> -->
    <xsl:param name="MetaTag" />
    <!--xsl:param name="GuidTag" /-->
    <div class="ico_container">
      <xsl:if test="$MetaTag/@has_gallery='true' or $MetaTag/@has_gallery='True'">
        <div class="ico_galeria"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
      </xsl:if>
      <xsl:if test="$MetaTag/@has_video='true' or $MetaTag/@has_video='True'">
        <div class="ico_video"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
      </xsl:if>
      <xsl:if test="$MetaTag/@has_audio='true' or $MetaTag/@has_audio='True'">
        <div class="ico_audio"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
      </xsl:if>
      <!-- xsl:if test="$MetaTag/@has_audio='false' and $MetaTag/@has_video='false' and $MetaTag/@has_gallery='false'">
        <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
      </xsl:if -->
      <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
    </div>
  </xsl:template>

  <!-- Template de la nota abierta con o sin imagen.-->
  <xsl:template name="NotaAbierta">
    <xsl:param name="Node" />
    <div id="nota">
      <xsl:choose>
        <xsl:when test="not(not($Node/thumbnail))">
          <div class="main_img_container">
            <img src="{$Node/thumbnail/@url}.i" id="img_{$Node/thumbnail/@url}" />
            <xsl:variable name="container_type">video_over_photo</xsl:variable>
            <xsl:call-template name="MediaLink">
              <xsl:with-param name="Node" select="$Node"/>
              <xsl:with-param name="container_type" select="$container_type"/>
            </xsl:call-template>
          </div>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="container_type">no_photo</xsl:variable>
          <xsl:call-template name="MediaLink">
            <xsl:with-param name="Node" select="$Node"/>
            <xsl:with-param name="container_type" select="$container_type"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
      
      <div class="contenido">
        <div id="titulo">
          <label>
            <xsl:call-template name="FormatDate">
              <xsl:with-param name="DateTime" select="$Node/pubDate"/>
            </xsl:call-template>
          </label> | <label class="seccion">
            <xsl:call-template name="ReplaceInfoGral">
              <xsl:with-param name="seccion" select="$Node/category"/>
            </xsl:call-template>
          </label>
          <br />
          <h1><xsl:value-of disable-output-escaping="yes" select="$Node/title" /></h1>
        </div>
        <xsl:if test="$Node/subheader and $Node/subheader!=''">
          <div class="bajada" id="bajada">
            <xsl:value-of disable-output-escaping="yes" select="$Node/subheader" />
          </div>
        </xsl:if>
        <div id="informacion" style="display:block;">
          <xsl:value-of disable-output-escaping="yes" select="$Node/content" />
        </div>
      </div>
    </div>
  </xsl:template>
  
  <!-- Template para permitir acceder a elementos multimedia de la noticia. -->
  <xsl:template name="MediaLink">
    <!-- <meta has_gallery="true" has_video="false" has_audio="false" /> -->
    <xsl:param name="Node" />
    <xsl:param name="container_type" />
    <xsl:if test="$Node/content[@type='audio'] or $Node/content[@type='audio/mpeg'] or $Node/group/content or $Node/content[@type='video']" >
      <div class="media_link {$container_type}">
        
        <xsl:if test="$Node/content[@type='audio']">
          <a class="ico_audio" href="audio://{$Node/content[@type='audio'][1]/@url}" title=""><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></a>
        </xsl:if>
        <xsl:if test="$Node/content[@type='audio/mpeg']">
          <a class="ico_audio" href="audio://{$Node/content[@type='audio/mpeg'][1]/@url}" title=""><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></a>
        </xsl:if>
        
        <xsl:if test="$Node/group/content">
          <xsl:variable name="gallery">
            <xsl:for-each select="$Node/group/content">
              <xsl:value-of select="concat(@url, ';')"/>
            </xsl:for-each>
          </xsl:variable>
          <a href="galeria://{$gallery}" title="galeria" class="ico_galeria"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></a>
        </xsl:if>
        
        <!--xsl:if test="$Node/thumbnail">
          <a href="galeria://{$Node/thumbnail/@url};{$Node/thumbnail/@url};{$Node/thumbnail/@url}" title="galeria" class="ico_galeria"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></a>
        </xsl:if-->
        
        <xsl:if test="$Node/content[@type='video']">
          <a class="ico_video" href="video://{$Node/content[@type='video'][1]/@url}" title=""><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></a>
        </xsl:if>
        
        
        <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
      
      </div>
    </xsl:if>
  </xsl:template>
  
  <!-- Template generador del link de las imegenes de la galeria. -->
  <!--xsl:template name="GalleryTemplate">
    <xsl:param name="media_group" />
    <xsl:for-each select="$media_group/content">
      <xsl:value-of select="concat( substring('; ','{@url}'),.)"/>
    </xsl:for-each>
  </xsl:template-->
  
  <!-- Template que arma el listado de noticias relacionadas. -->
  <xsl:template name="ListadoNoticiasRelacionadas">
    <xsl:param name="Items" />
    <div id="listado" style="display:block;">
      <ul class="main_list">
        <xsl:for-each select="$Items">
          <xsl:call-template name="NoticiaRelacionada">
            <xsl:with-param name="Item" select="."/>
          </xsl:call-template>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:template>
  
  <!-- Template de la noticia en listado de noticias relacionadas (ListadoNoticiasRelacionadas). -->  
  <xsl:template name="NoticiaRelacionada">
    <xsl:param name="Item" />
    
    <xsl:variable name="has_image" select="$Item/@thumbnail!=''"></xsl:variable>
    <xsl:variable name="full_width" >
      <xsl:if test="not($has_image)">
        <xsl:text>full_width</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="encoded_url" >
      <xsl:call-template name="url-encode">
        <xsl:with-param name="str" select="$Item/@url"/>
      </xsl:call-template>
    </xsl:variable>
    <li>
      <a href="noticia://{$Item/@guid}?url={$encoded_url}&amp;title={$Item/.}&amp;header=" title="">
        <div class="titular {$full_width}">
          <label>
            <xsl:call-template name="FormatDate">
              <xsl:with-param name="DateTime" select="$Item/@pubDate"/>
            </xsl:call-template>
          </label> 
          <xsl:if test="$Item/@lead!=''">
            <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>|<xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
            <label class="seccion">
              <xsl:call-template name="ReplaceInfoGral">
                <xsl:with-param name="seccion" select="$Item/@lead"/>
              </xsl:call-template>
              <!--xsl:value-of disable-output-escaping="yes" select="$Item/@lead" /--></label>
          </xsl:if>
          <br />
          <label class="titulo"><xsl:value-of disable-output-escaping="yes" select="$Item/." /></label>
        </div>
        
        <xsl:if test="not(not($has_image))">
          <div class="foto img_container">
            <xsl:call-template name="MediaAttach">
              <xsl:with-param name="MetaTag" select="$Item/meta"/>
            </xsl:call-template>
            <xsl:if test="not(not($Item/@thumbnail))">
              <xsl:if test="$Item/@thumbnail!=''">
                <div class="imagen_secundaria" id="{$Item/@thumbnail}" style="background-image:url({$Item/@thumbnail}.i) !important;"></div>
                <div class="img_loader"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
              </xsl:if>
            </xsl:if>
            <!--xsl:if test="not($Item/@thumbnail) or $Item/@thumbnail=''">
              <xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
            </xsl:if-->
          </div>
        </xsl:if>
        
        <xsl:if test="not($has_image)">
          <xsl:if test="not(not($Item/meta))">
            <div class="right_ico_container">
              <xsl:call-template name="MediaAttach">
                <xsl:with-param name="MetaTag" select="$Item/meta"/>
              </xsl:call-template>
            </div>
          </xsl:if>
        </xsl:if>
      </a>
      <div class="separador"><xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text></div>
    </li>
  </xsl:template>
  
  <!-- Template para el titulo de seccion o el header de las noticias relacionadas. -->
  <xsl:template name="TituloSeccionONotisRelac">
    <xsl:param name="Titulo" />
    <div id="titulo_seccion"><label class="lbl_titulo_seccion"><xsl:value-of disable-output-escaping="yes" select="$Titulo" /></label></div>
  </xsl:template>
  
  <xsl:template name="ReplaceInfoGral">
    <xsl:param name="seccion" />
    <xsl:variable name="replace">Información General</xsl:variable>
    <xsl:variable name="by">Información Gral</xsl:variable>
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$seccion" />
        <xsl:with-param name="replace" select="$replace" />
        <xsl:with-param name="by"  select="$by" />
      </xsl:call-template>
   </xsl:template>

  <xsl:template name="string-replace-all">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="by" />
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)" />
        <xsl:value-of select="$by" />
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text"
          select="substring-after($text,$replace)" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="by" select="$by" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of disable-output-escaping="yes" select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
