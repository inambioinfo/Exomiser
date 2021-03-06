/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.db.io;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 *
 * @author jj8
 */
@RunWith(MockitoJUnitRunner.class)
public class PhenodigmDataDumperTest {

    private static final Path outputPath = Paths.get("target/test-data");
    private static final File outputDir = outputPath.toFile();

    @InjectMocks
    private PhenodigmDataDumper instance;
  
    @Mock
    DataSource mockDataSource;

    @BeforeClass
    public static void setUpClass() {
        outputDir.mkdir();
        //clean up all files
        for (File file : outputDir.listFiles()) {
            file.delete();
        }
        assertTrue(outputDir.isDirectory());
        assertEquals("Expected output path to be empty before tests start", 0, outputDir.listFiles().length);

    }

    @Test
    public void testIsNotNull() {
        assertThat(instance, notNullValue());
    }
    
//    /**
//     * Test of dumpPhenodigmData method, of class PhenodigmDataDumper.
//     */
//    @Test
//    public void testDumpPhenodigmData() {
//
//        instance.dumpPhenodigmData(outputPath);
//        File outputFile = outputPath.toFile();
//        assertTrue("Expected output path to be a directory", Files.isDirectory(outputPath));
//        assertNotEquals("Expected output path to contain some files!", 0, outputFile.listFiles().length);
//
//        List<File> expectedFiles = new ArrayList<>();
//        expectedFiles.add(new File(outputPath.toFile(), "human2mouseOrthologs.pg"));
//        expectedFiles.add(new File(outputPath.toFile(), "diseaseHp.pg"));
//        expectedFiles.add(new File(outputPath.toFile(), "mouseMp.pg"));
//        expectedFiles.add(new File(outputPath.toFile(), "diseaseDisease.pg"));
//        expectedFiles.add(new File(outputPath.toFile(), "omimTerms.pg"));
//        expectedFiles.add(new File(outputPath.toFile(), "hpMpMapping.pg"));
//        expectedFiles.add(new File(outputPath.toFile(), "hpHpMapping.pg"));
//        expectedFiles.add(new File(outputPath.toFile(), "orphanet.pg"));
//
//        int expectedNoFiles = expectedFiles.size();
//        assertEquals("Wrong number of files in output directory", expectedNoFiles, outputDir.listFiles().length);
//
//        for (File file : expectedFiles) {
//            assertTrue(file.exists());
//        }
//
//        for (File file : outputFile.listFiles()) {
//            assertNotEquals(0, file.length());
//        }
//    }
//
//    @Test
//    public void testDumpDiseaseDiseaseSummary() {
//        File expectedFile = PhenodigmDataDumper.dumpDiseaseDiseaseSummary(outputPath, "testDumpDiseaseDisease.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//
//    @Test
//    public void testDumpOmimTerms() {
//        File expectedFile = instance.dumpOmimTerms(outputPath, "testDumpOmim.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//
//    @Test
//    public void testDumpHpHpMapping() {
//        File expectedFile = instance.dumpHpHpMapping(outputPath, "testDumpHpHp.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//
//    @Test
//    public void testDumpHpMpMapping() {
//        File expectedFile = instance.dumpHpMpMapping(outputPath, "testDumpHpMp.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//
//    @Test
//    public void testDumpDiseaseHp() {
//        File expectedFile = instance.dumpDiseaseHp(outputPath, "testDumpDiseaseHp.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//
//    @Test
//    public void testDumpMouseMp() {
//        File expectedFile = instance.dumpMouseMp(outputPath, "testDumpMouseMp.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//
//    @Test
//    public void testDumpMouseGeneOrthologData() {
//        File expectedFile = instance.dumpMouseGeneOrthologs(outputPath, "testDumpMouseGeneOrtholog.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//    
//    @Test
//    public void testDumpFishGeneOrthologs() {
//        File expectedFile = instance.dumpFishGeneOrthologs(outputPath, "testDumpFishGeneOrtholog.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
//    
//    @Test
//    public void testDumpOrphanet() {
//        File expectedFile = instance.dumpOrphanet(outputPath, "testDumpOrphanet.pg");
//        assertTrue(expectedFile.exists());
//        assertNotEquals(0, expectedFile.length());
//    }
}
