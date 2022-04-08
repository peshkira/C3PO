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
package com.petpet.c3po.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.RemoveParams;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

/**
 * Submits a remove request to the controller based on the passed parameters.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class RemoveCommand extends AbstractCLICommand {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( RemoveCommand.class );

  /**
   * The remove paramteres passed on the command line.
   */
  private RemoveParams params;

  @Override
  public void setParams( Params params ) {
    if ( params != null && params instanceof RemoveParams ) {
      this.params = (RemoveParams) params;
    }
  }

  /**
   * Prompts for confirmation and submits a remove request to the controller.
   */
  @Override
  public void execute() {
    String collection = this.params.getCollection();
    boolean proceed = this.prompt( collection );
    if ( !proceed ) {
      System.out.println( "Oh, my! Collection names do not match.\nStopping collection removal." );
      return;
    }

    final long start = System.currentTimeMillis();

    final Configurator configurator = Configurator.getDefaultConfigurator();
    //configurator.configure();

    Controller ctrl = new Controller( configurator );

    Map<String, Object> options = new HashMap<String, Object>();
    options.put( Constants.OPT_COLLECTION_NAME, collection );

    try {
      ctrl.removeCollection( options );
    } catch ( C3POConfigurationException e ) {
      LOG.error( e.getMessage() );
      return;

    } finally {
      cleanup();
    }

    final long end = System.currentTimeMillis();
    this.setTime( end - start );
  }

  /**
   * Prompts the user to confirm, which collection should be removed. The user
   * has to write the exact name of the collection and hit enter in order to
   * confirm.
   * 
   * @param name
   *          the name of the collection to remove.
   * @return true if the typed in name equals the collection name from the
   *         request.
   */
  private boolean prompt( String name ) {
    System.out.println( "Are you sure you want to remove all elements from collection " + name
        + "?\nPlease type in the collection name again and hit Enter!" );
    Scanner scanner = new Scanner( System.in );
    String next = scanner.next();

    return (next.equals( name ));
  }

}
