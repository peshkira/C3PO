/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.common;

public final class Constants {

  /**
   * The url for the xml schema property used by the sax parser while validating
   * xml files against their schemata.
   */
  public static final String XML_SCHEMA_PROPERTY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  /**
   * The url for the xml schema language used by the sax parser while validating
   * xml files against their schemata.
   */
  public static final String XML_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

  /**
   * A c3po configuration for the collection on which to operate.
   */
  public static final String OPT_COLLECTION_NAME = "c3po.collection.name";

  /**
   * A c3po configuration for the location where the metadata is.
   */
  public static final String OPT_COLLECTION_LOCATION = "c3po.collection.location";

  /**
   * A c3po configuration for the type of the input files. Currently only FITS
   * and TIKA are supported. This config is required for the controller to
   * operate.
   */
  public static final String OPT_INPUT_TYPE = "c3po.input.type";

  /**
   * A configuartion for recursive processing.
   */
  public static final String OPT_RECURSIVE = "c3po.recursive";

  /**
   * A configuration option for the sampling algorithm to use.
   */
  public static final String OPT_SAMPLING_ALGORITHM = "c3po.sampling.alogrithm";

  /**
   * A configuration option for the size of the sample set.
   */
  public static final String OPT_SAMPLING_SIZE = "c3po.sampling.size";

  /**
   * A configuration option for the properties over which some sample algorithms
   * should work.
   */
  public static final String OPT_SAMPLING_PROPERTIES = "c3po.sampling.properties";

  /**
   * A configuration option for including the element identifiers.
   */
  public static final String OPT_INCLUDE_ELEMENTS = "c3po.profile.includeelements";

  /**
   * A configuration option for the output location of a CLI command.
   */
  public static final String OPT_OUTPUT_LOCATION = "c3po.cli.outputlocation";

  /**
   * A config identifier for the persistence layer class.
   */
  public static final String CNF_PERSISTENCE = "c3po.persistence";

  /**
   * The thread count configuration for the adaptor workers.
   */
  public static final String CNF_ADAPTORS_COUNT = "c3po.controller.adaptors.count";

  /**
   * The thread count configuration for the consolidator workers.
   */
  public static final String CNF_CONSOLIDATORS_COUNT = "c3po.controller.consolidators.count";

  /**
   * The key for the create element identifier processing rule.
   */
  public static final String CNF_ELEMENT_IDENTIFIER_RULE = "c3po.rule.create_element_identifier";

  /**
   * The key for the empty value processing rule.
   */
  public static final String CNF_EMPTY_VALUE_RULE = "c3po.rule.empty_value_processing";

  /**
   * The key for the format version resolution processing rule.
   */
  public static final String CNF_VERSION_RESOLUTION_RULE = "c3po.rule.format_version_resolution";

  /**
   * The key for the html info processing rule.
   */
  public static final String CNF_HTML_INFO_RULE = "c3po.rule.html_info_processing";

  /**
   * The key for the infer date processing rule.
   */
  public static final String CNF_INFER_DATE_RULE = "c3po.rule.infer_date_from_file_name";
  
  /**
   * The key for the Browsershot Dissimilarity Count Rule 
   */
  public static final String CNF_BROWSERSHOT_DISSIMILARITY_COUNT_RULE = "c3po.rule.browsershot_dissimilarity_count";

  /**
   * The key for the drools conflict resolution processing rule.
   */
  public static final String CNF_DROOLS_CONFLICT_RESOLUTION_RULE = "c3po.rule.drools_conflict_resolution_processing";

  public static final String CNF_DROOLS_PATH="c3po.rule.drools_path";

  /**
   * The key for the content type indentification rule.
   */
  public static final String CNF_CONTENT_TYPE_IDENTIFICATION_RULE = "c3po.rule.content_type_identification";

  /**
   * The key for the file extension indentification rule.
   */
  public static final String CNF_FILE_EXTENSION_IDENTIFICATION_RULE = "c3po.rule.file_extension_identification";

  /**
   * The key for the created year indentification rule.
   */
  public static final String CNF_CREATED_YEAR_IDENTIFICATION_RULE = "c3po.rule.created_year_identification";

  /**
   * An array of the configurable processing rule keys.
   */
  public static final String[] RULE_KEYS = { CNF_ELEMENT_IDENTIFIER_RULE, CNF_VERSION_RESOLUTION_RULE,
      CNF_EMPTY_VALUE_RULE, CNF_INFER_DATE_RULE, CNF_HTML_INFO_RULE,  CNF_BROWSERSHOT_DISSIMILARITY_COUNT_RULE , CNF_DROOLS_CONFLICT_RESOLUTION_RULE, CNF_CONTENT_TYPE_IDENTIFICATION_RULE, CNF_FILE_EXTENSION_IDENTIFICATION_RULE, CNF_CREATED_YEAR_IDENTIFICATION_RULE};

  /**
   * The version of the core module.
   */
  public static final String CORE_VERSION = "0.5.0-SNAPSHOT";

  /**
   * The version of the api module.
   */
  public static final String API_VERSION = "0.5.0-SNAPSHOT";


  /**
   * The same as {@link Constants#AGGREGATE_MAP} but it aggregates the desired
   * property only for elements where the passed filter has a specific value.
   * {1} - the filter property id (e.g. 'mimetype') {2} - the value of the
   * filter (e.g. 'application/pdf') {3} - the property to aggregate (e.g.
   * 'size')
   */
  @Deprecated
  public static final String FILTER_AGGREGATE_MAP = "function map() {if (this.metadata['{1}'].value === '{2}') {emit(1,{sum: this.metadata['{3}'].value, min: this.metadata['{3}'].value,max: this.metadata['{3}'].value,count:1,diff: 0,});}}";

  @Deprecated
  public static final String PROPERTIES_IN_COLLECTION_MAP = "function map() {for (var key in this) {if (key == 'metadata') {for (var subkey in this[key]) {emit(subkey, null);}}}}";

  @Deprecated
  public static final String PROPERTIES_IN_COLLECTION_REDUCE = "function reduce(key, values) {return null;}";


  private Constants() {

  }
}
