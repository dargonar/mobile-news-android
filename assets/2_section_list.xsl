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
        <!-- meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, user-scalable=no, width=device-width" / -->
        <link rel="stylesheet" type="text/css" href="css/layout.css" />
        <!-- link rel="stylesheet" type="text/css" media="only screen and (max-device-width: 480px)" href="css/layout.css" / -->
        <title>SECCIONES</title>
        <script type="text/javascript" src="js/functions.js"></script>
        <script type="text/javascript">
			//meta_head_trick();
        </script>        
        <meta name="viewport" content="width=device-width, user-scalable=no" />
      </head>
      
      <body>
        <xsl:call-template name="TituloSeccionONotisRelac">
          <xsl:with-param name="Titulo" select="rss/channel/item[1]/category"/>
        </xsl:call-template>
        
        <xsl:call-template name="ListadoNoticiasEnListado">
          <xsl:with-param name="Nodes" select="rss/channel/item"/>
        </xsl:call-template>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>