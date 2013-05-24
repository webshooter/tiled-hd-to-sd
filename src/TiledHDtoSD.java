/**
 * 
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 

/**
 * @author randall
 *
 */
public class TiledHDtoSD {

	private static String tmxFileName;
	private static File tmxFile;
	
	private static final String TMX_HD_SUFFIX = "-ipadhd.tmx";
	private static final String TMX_SD_SUFFIX = "-ipad.tmx";
	private static final String TSX_HD_SUFFIX = "-ipadhd.tsx";
	private static final String TSX_SD_SUFFIX = "-ipad.tsx";
	private static final String HD_SUFFIX = "-ipadhd";
	private static final String SD_SUFFIX = "-ipad";
	private static final String TILESET_TAGNAME = "tileset";
	private static final String SOURCE_ATTR_NAME = "source";
	private static final String TILEWIDTH_ATTR_NAME = "tilewidth";
	private static final String TILEHEIGHT_ATTR_NAME = "tileheight";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 1) {
			showSyntaxMessage();
			return;
		}
		
		tmxFileName = args[0];
		
		if (!tmxFileName.contains(TMX_HD_SUFFIX))
		{
			out("The file '" + tmxFileName + "' doesn't have the expected '-ipadhd.tmx' suffix. Unable to process.");
			return;
		}
		
		tmxFile = new File(tmxFileName);
		
		if (!tmxFile.exists())
		{
			out("Unable to find file '" + tmxFileName + "'. Check the path and try again.");
			return;
		}
		
		File sdTmxFile = new File(tmxFileName.replace(TMX_HD_SUFFIX, TMX_SD_SUFFIX));
		
		try
		{
			// load the xml document
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(tmxFile);
			doc.getDocumentElement().normalize();
		
			
			// first, change the map element's tilewidth and tileheight properties
			Element root = doc.getDocumentElement();
			if (root.hasAttribute(TILEWIDTH_ATTR_NAME) && root.hasAttribute(TILEHEIGHT_ATTR_NAME))
			{
				String tilewidthStringValue = root.getAttribute(TILEWIDTH_ATTR_NAME);
				String tileheightStringValue = root.getAttribute(TILEHEIGHT_ATTR_NAME);
				int tilewidthValue = Integer.parseInt(tilewidthStringValue);
				int tileheightValue = Integer.parseInt(tileheightStringValue);
				tilewidthValue = tilewidthValue/2;
				tileheightValue = tileheightValue/2;
				root.setAttribute(TILEWIDTH_ATTR_NAME, String.valueOf(tilewidthValue));
				root.setAttribute(TILEHEIGHT_ATTR_NAME, String.valueOf(tileheightValue));
			}
			else
			{
				out("WARNING: The 'map' tag in the TMX file '" + tmxFileName + "' seems to be missing the 'tilewidth' or 'tileheight' attributes. Is it malformed?");
			}
			
			// then change the tileset source filename references
			NodeList tileSetNodes = doc.getElementsByTagName(TILESET_TAGNAME);
			for (int i=0; i<tileSetNodes.getLength(); i++)
			{
				Node nodeTileSet = tileSetNodes.item(i);
				if (nodeTileSet.getNodeType() == Node.ELEMENT_NODE)
				{
					Element elemTileSet = (Element) nodeTileSet;
					
					if (elemTileSet.hasAttribute(SOURCE_ATTR_NAME))
					{
						String attrSourceValue = elemTileSet.getAttribute(SOURCE_ATTR_NAME);
						
						if (attrSourceValue.contains(TSX_HD_SUFFIX))
						{
							attrSourceValue = attrSourceValue.replace(TSX_HD_SUFFIX, TSX_SD_SUFFIX);
							elemTileSet.setAttribute(SOURCE_ATTR_NAME, attrSourceValue);
						}
						else
						{
							out("WARNING: The tileset file '" + attrSourceValue + "' specified in the TMX file '" + tmxFileName + "' does not have the '-ipadhd.tsx' suffix! No change made.");
						}
					}
					else
					{
						out("WARNING: A tileset element in the TMX file '" + tmxFileName + "' does not have a 'source' attribute! No change made.");
					}	
				}
			}
		
			// Write the TMX doc out to the SD file
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(sdTmxFile)));
			transformer.transform(source, result); 
		
		}
		catch (Exception e)
		{
			out(e.getMessage());
		}

	}
	
	private static String getAttributeValue(final Element element)
    {
        System.out.println(element.getTagName() + " has attributes: " + element.hasAttributes());

        if (element.getTagName().startsWith("test"))
        {
            return element.getAttribute("w");

        }
        else
        {
            return element.getNodeValue();
        }
    }
	
	private static void showSyntaxMessage() {
		
		out("TiledHDtoSD Syntax:");
		out("-------------------");
		
	}
	
	private static void out(String msg)
	{
		System.out.println(msg);
	}

}
