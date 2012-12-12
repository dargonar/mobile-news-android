package com.diventi.mobipaper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.icl.saxon.trax.Processor;
import com.icl.saxon.trax.ProcessorException;
import com.icl.saxon.trax.Result;
import com.icl.saxon.trax.Templates;
import com.icl.saxon.trax.Transformer;
import android.content.res.AssetManager;

public class HtmlGenerator {

  private static String TAG  = "HtmlGenerator";
  
  public byte[] generate(byte[] xml, String xsl, String encoding) throws IOException, ProcessorException, SAXException  {

    AssetManager assets = MobiPaperApp.getContext().getAssets();
    
    Processor processor = Processor.newInstance("xslt");
      
    InputSource xmlInputSource  = new InputSource( new InputStreamReader(new ByteArrayInputStream(xml), encoding) );
    InputSource xsltInputSource = new InputSource( assets.open(xsl) );
 
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Result result = new Result(output);
    
    // create a new compiled stylesheet
    Templates templates = processor.process(xsltInputSource);
    
    // create a transformer that can be used for a single transformation
    Transformer trans = templates.newTransformer( );
    trans.transform(xmlInputSource, result);
    
    return output.toByteArray();
  }
   
}
