/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.functional;

/**
 * Created by gabm on 19/11/15.
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.location.Location;

import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.data.ImageFile;

public class GpxFileContentHandler implements ContentHandler {
    private String currentValue;
    private FancyPlace curFancyPlace;
    private List<FancyPlace> curFancyPlaceList;
    private boolean isInsideFPTag;

    public GpxFileContentHandler() {
        curFancyPlaceList = new ArrayList<>();
        isInsideFPTag = false;
    }

    public List<FancyPlace> getFancyPlaceList()
    {
        return curFancyPlaceList;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes atts) throws SAXException {

        if (localName.equalsIgnoreCase("wpt") && !isInsideFPTag) {
            curFancyPlace = new FancyPlace();
            curFancyPlace.setLocationLat(atts.getValue("lat").trim());
            curFancyPlace.setLocationLong(atts.getValue("lon").trim());
            isInsideFPTag = true;
        }

        if (localName.equalsIgnoreCase("link") && isInsideFPTag)
        {
            // todo: maybe well have to change that
            ImageFile tmpImg = new ImageFile(atts.getValue("href").trim());
            curFancyPlace.setImage(tmpImg);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (localName.equalsIgnoreCase("name") && isInsideFPTag)
            curFancyPlace.setTitle(currentValue.trim());

        if (localName.equalsIgnoreCase("desc") && isInsideFPTag )
            curFancyPlace.setNotes(currentValue.trim());

        if (localName.equalsIgnoreCase("wpt") && isInsideFPTag)
        {
            curFancyPlaceList.add(curFancyPlace);
            isInsideFPTag = false;
        }

    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        currentValue = new String(ch, start, length);
    }

    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // TODO Auto-generated method stub
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        // TODO Auto-generated method stub
    }

}
