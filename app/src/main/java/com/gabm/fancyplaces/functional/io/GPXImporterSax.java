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

package com.gabm.fancyplaces.functional.io;

import com.gabm.fancyplaces.FancyPlacesApplication;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.functional.Utilities;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabm on 19/11/15.
 */
public class GPXImporterSax implements IImporter {
    @Override
    public List<FancyPlace> ReadFancyPlaces(String fileName) {

        File tmpFolder = new File(FancyPlacesApplication.TMP_FOLDER + File.separator + "import");
        tmpFolder.mkdirs();
        List<FancyPlace> result = new ArrayList<>();
        if (!Compression.unzip(fileName, tmpFolder.getAbsolutePath()))
            return result;

        result = ReadGXPFile(new File(tmpFolder.getAbsolutePath() + File.separator + "FancyPlaces.gpx"));


        Utilities.deleteRecursive(tmpFolder);

        return result;
    }



    List<FancyPlace> ReadGXPFile(File file)
    {
        List<FancyPlace> resultList = new ArrayList<>();

        try {
            System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();

            GpxFileContentHandler gpxFileContentHandler = new GpxFileContentHandler(file.getParent());
            xmlReader.setContentHandler(gpxFileContentHandler);

            FileReader fileReader = new FileReader(file);
            InputSource inputSource = new InputSource(fileReader);
            xmlReader.parse(inputSource);

            resultList = gpxFileContentHandler.getFancyPlaceList();
        } catch (SAXException | IOException ex) {
            ex.printStackTrace();
        }

        return resultList;
    }
}
