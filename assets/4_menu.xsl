<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:media="http://search.yahoo.com/mrss/"
  xmlns:news="http://www.diariosmoviles.com.ar/news-rss/">
  
  <xsl:include href="file://android_asset/functions.xsl" />
  <xsl:output method="html" 
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" 
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" indent="yes" encoding="UTF-8"/>
  
  <xsl:template match="/">
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link rel="stylesheet" type="text/css" href="css/layout.css" />
        <title>MENU</title>
        <script type="text/javascript" src="js/functions.js"></script>
        <script type="text/javascript">
          //meta_head_trick();
        </script>
          <meta name="viewport" content="width=device-width, user-scalable=no" />
      </head>
      
      <body style="width:100% !important">
        <div id="menu">
          <div class="menu-header">Secciones</div>
          <ul>
            <li><a href="section://main">Principal</a></li>
            <xsl:for-each select="rss/channel/item">
              <li><a href="section://{guid}"><xsl:value-of disable-output-escaping="yes" select="title" /></a></li>
            </xsl:for-each>
          
            <li>
				<a class="vip2" href="page://clasifieds.html">Servicios y clasificados</a>
   			</li>
			<li class="vip2_close"></li>
          
          </ul>
		</div>
      </body>
	</html>
      
  </xsl:template>

</xsl:stylesheet>