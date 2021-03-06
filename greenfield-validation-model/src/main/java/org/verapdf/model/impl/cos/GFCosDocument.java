package org.verapdf.model.impl.cos;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.model.baselayer.Object;
import org.verapdf.model.coslayer.CosDocument;
import org.verapdf.model.coslayer.CosIndirect;
import org.verapdf.model.coslayer.CosTrailer;
import org.verapdf.model.coslayer.CosXRef;
import org.verapdf.pd.PDDocument;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GFCosDocument extends GFCosObject implements CosDocument {

    private static final Logger LOGGER = Logger.getLogger(GFCosDocument.class);

    /** Type name for GFCosDocument */
    public static final String COS_DOCUMENT_TYPE = "CosDocument";

    public static final String TRAILER = "trailer";
    public static final String XREF = "xref";
    public static final String INDIRECT_OBJECTS = "indirectObjects";
    public static final String DOCUMENT = "document";
    public static final String EMBEDDED_FILES = "EmbeddedFiles";
    public static final String ID = "ID";
    public static final String REQUIREMENTS = "Requirements";

    private final PDFAFlavour flavour;

    private PDDocument pdDocument;

    private final COSDictionary catalog;

    private final float version;
    private final long headerOffset;
    private final String header;
    private final int headerCommentByte1;
    private final int headerCommentByte2;
    private final int headerCommentByte3;
    private final int headerCommentByte4;

    /**
     * Default constructor
     * @param pdDocument greenfield PDDocument
     */
    public GFCosDocument(PDDocument pdDocument, PDFAFlavour flavour) {
        this(pdDocument.getDocument(), flavour);
        this.pdDocument = pdDocument;
    }

    /**
     * Constructor using greenfield COSDocument
     * @param cosDocument greenfield COSDocument
     */
    public GFCosDocument(COSDocument cosDocument, PDFAFlavour flavour) {
        super(cosDocument, COS_DOCUMENT_TYPE);
        this.catalog = this.getCatalog();
        this.flavour = flavour;

        COSHeader cosHeader = cosDocument.getHeader();
        this.version = cosHeader.getVersion();
        this.headerOffset = cosHeader.getHeaderOffset();
        this.header = cosHeader.getHeader();
        this.headerCommentByte1 = cosHeader.getHeaderCommentByte1();
        this.headerCommentByte2 = cosHeader.getHeaderCommentByte2();
        this.headerCommentByte3 = cosHeader.getHeaderCommentByte3();
        this.headerCommentByte4 = cosHeader.getHeaderCommentByte4();
    }

    /**
     * Number of indirect objects in the document
     */
    @Override
    public Long getnrIndirects() {
        return null;
    }

    /**
     * @return version of pdf document
     */
    @Override
    public Double getversion() {
        return Double.valueOf(this.version);
    }

    @Override
    public Long getheaderOffset() {
        return this.headerOffset;
    }

    @Override
    public String getheader() {
        return this.header;
    }

    @Override
    public Long getheaderByte1() {
        return Long.valueOf(this.headerCommentByte1);
    }

    @Override
    public Long getheaderByte2() {
        return Long.valueOf(this.headerCommentByte2);
    }

    @Override
    public Long getheaderByte3() {
        return Long.valueOf(this.headerCommentByte3);
    }

    @Override
    public Long getheaderByte4() {
        return Long.valueOf(this.headerCommentByte4);
    }

    /**
     * true if catalog contain OCProperties key
     */
    @Override
    public Boolean getisOptionalContentPresent() {
        return null;
    }

    /**
     * EOF must complies PDF/A standard
     */
    @Override
    public Long getpostEOFDataSize() {
        return null;
    }

    /**
     * @return ID of first page trailer
     */
    @Override
    public String getfirstPageID() {
        return null;
    }

    /**
     * @return ID of last document trailer
     */
    @Override
    public String getlastID() {
        return null;
    }

    /**
     * @return true if the current document is linearized
     */
    @Override
    public Boolean getisLinearized() {
        return null;
    }

    /**
     * @return true if XMP content matches Info dictionary content
     */
    @Override
    public Boolean getdoesInfoMatchXMP() {
        return null;
    }

    @Override
    public Boolean getMarked() {
        if (this.catalog != null) {
            COSObject markInfoObject = this.catalog.getKey(ASAtom.MARK_INFO);
            if (markInfoObject == null) {
                return Boolean.FALSE;
            } else {
                COSBase markInfo = markInfoObject.get();
                if (markInfo instanceof COSDictionary) {
                    return markInfo.getBooleanKey(new ASAtom("Marked"));
                } else {
                    LOGGER.warn("MarkedInfo must be a 'COSDictionary' but got: "
                            + markInfoObject.getClass().getSimpleName());
                    return Boolean.FALSE;
                }
            }
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public String getRequirements() {
        if (this.catalog != null) {
            COSObject reqArrayObject = this.catalog.getKey(new ASAtom(REQUIREMENTS));
            if (reqArrayObject != null) {
                COSBase reqArray = reqArrayObject.get();
                if (reqArray instanceof COSArray) {
                    return this.getRequirementsString((COSArray) reqArray);
                }
            }
        }
        return null;
    }

    private String getRequirementsString(COSArray reqArray) {
        String result = "";
        Iterator iterator = reqArray.iterator();
        while (iterator.hasNext()) {
            COSBase element = (COSBase) iterator.next();
            if (element instanceof COSDictionary) {
                String sKey = element.getStringKey(ASAtom.S);
                result += sKey;
                result += " ";
            }
        }
        return result;
    }

    /**
     * @return true if {@code NeedsRendering} entry contains {@code true} value
     */
    @Override
    public Boolean getNeedsRendering() {
        return null;
    }

    @Override
    public List<? extends Object> getLinkedObjects(String link) {
        switch (link) {
            case TRAILER:
                return this.getTrailer();
            case INDIRECT_OBJECTS:
                return this.getIndirectObjects();
            case DOCUMENT:
                return this.getDocument();
            case XREF:
                return this.getXRefs();
            case EMBEDDED_FILES:
                return this.getEmbeddedFiles();
            default:
                return super.getLinkedObjects(link);
        }
    }

    /**
     * @return list of embedded files
     */
    private List<Object> getEmbeddedFiles() {
        return Collections.emptyList();
    }

    /**
     * trailer dictionary
     */
    private List<CosTrailer> getTrailer() {
        List<CosTrailer> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
        list.add(new GFCosTrailer((COSDictionary) cosDocument.getTrailer().getObject().get(), this.pdDocument, this.flavour));
        return Collections.unmodifiableList(list);
    }

    /**
     * all indirect objects referred from the xref table
     */
    private List<CosIndirect> getIndirectObjects() {
        List<COSObject> objects = cosDocument.getObjects();
        List<CosIndirect> list = new ArrayList<>(objects.size());
        for (COSObject object : objects) {
            if (object.isIndirect()) {
                list.add(new GFCosIndirect((COSIndirect) object.get(), this.pdDocument, this.flavour));
            }
            //TODO : check if always indirect
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * link to the high-level PDF Document structure
     */
    private List<org.verapdf.model.pdlayer.PDDocument> getDocument() {
        return Collections.emptyList();
    }

    /**
     * link to cross reference table properties
     */
    private List<CosXRef> getXRefs() {
        List<CosXRef> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
        list.add(new GFCosXRef(cosDocument.isSubsectionHeaderSpaceSeparated(),
                               cosDocument.isXrefEOLMarkersComplyPDFA()));
        return Collections.unmodifiableList(list);
    }

    private COSDictionary getCatalog() {
        COSBase catalogLocal = cosDocument.getTrailer().getRoot().get();
        return catalogLocal instanceof COSDictionary ? (COSDictionary) catalogLocal : null;
    }

}
