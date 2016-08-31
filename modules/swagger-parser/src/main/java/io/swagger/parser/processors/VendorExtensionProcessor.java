package io.swagger.parser.processors;

import static io.swagger.parser.util.RefUtils.isAnExternalRefFormat;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.models.Model;
import io.swagger.models.refs.GenericRef;
import io.swagger.models.refs.RefFormat;
import io.swagger.models.refs.RefType;
import io.swagger.parser.ResolverCache;

public class VendorExtensionProcessor {
	
	private final ExternalRefProcessor externalRefProcessor;
	private final ResolverCache cache;
	
	public VendorExtensionProcessor(ResolverCache cache, ExternalRefProcessor externalRefProcessor) {
		this.externalRefProcessor = externalRefProcessor;
		this.cache = cache;
	}
	
	public void processRefsFromVendorExtensions(Model model, String externalFile) {
        Map<String, Object> vendorExtensions = model.getVendorExtensions();
        if (vendorExtensions != null) {
            if (vendorExtensions.containsKey("x-collection")) {
                ObjectNode xCollection = (ObjectNode) vendorExtensions.get("x-collection");
                if (xCollection.has("schema") && xCollection.get("schema").has("$ref")) {
                    String sub$ref = xCollection.get("schema").get("$ref").asText();
                    GenericRef subRef = new GenericRef(RefType.DEFINITION, sub$ref);
                    if (isAnExternalRefFormat(subRef.getFormat())) {
                    	((ObjectNode) xCollection.get("schema")).put("$ref", "#/definitions/" + externalRefProcessor.processRefToExternalDefinition(subRef.getRef(), subRef.getFormat()));
                    } else if (externalFile != null) {
                    	externalRefProcessor.processRefToExternalDefinition(externalFile + subRef.getRef(), RefFormat.RELATIVE);
                    } else {
                    	cache.checkInternalRef(subRef.getRef());
                    }
                }
            }
            if (vendorExtensions.containsKey("x-links")) {
                ObjectNode xLinks = (ObjectNode) vendorExtensions.get("x-links");
                Iterator<String> xLinksNames = xLinks.fieldNames();
                while (xLinksNames.hasNext()) {
                    String linkName = xLinksNames.next();
                    if (xLinks.get(linkName) instanceof ObjectNode) {
                        ObjectNode xLink = (ObjectNode) xLinks.get(linkName);
                        if (xLink.has("schema") && xLink.get("schema").has("$ref")) {
                            String sub$ref = xLink.get("schema").get("$ref").asText();
                            GenericRef subRef = new GenericRef(RefType.DEFINITION, sub$ref);
                            if (isAnExternalRefFormat(subRef.getFormat())) {
                                ((ObjectNode) xLink.get("schema")).put("$ref", "#/definitions/" + externalRefProcessor.processRefToExternalDefinition(subRef.getRef(), subRef.getFormat()));
                            } else if (externalFile != null) {
                            	externalRefProcessor.processRefToExternalDefinition(externalFile + subRef.getRef(), RefFormat.RELATIVE);
                            } else {
                            	cache.checkInternalRef(subRef.getRef());
                            } 
                        }
                    }
                }
            }
        }
    }

}
