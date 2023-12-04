/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.calypsonet.keyple.demo.reload.remote.di

import dagger.Module
import dagger.Provides
import org.calypsonet.keyple.demo.reload.remote.data.ReaderRepository
import org.calypsonet.keyple.demo.reload.remote.data.network.KeypleSyncEndPointClient
import org.calypsonet.keyple.demo.reload.remote.di.scopes.AppScoped
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keyple.distributed.LocalServiceClient
import org.eclipse.keyple.distributed.LocalServiceClientFactoryBuilder

@Suppress("unused")
@Module
class ReaderModule {

  @Provides
  @AppScoped
  fun provideLocalServiceClient(
      keypleSyncEndPointClient: KeypleSyncEndPointClient
  ): LocalServiceClient {
    val smartCardService = SmartCardServiceProvider.getService()
    val localService =
        smartCardService.getDistributedLocalService("localService")
            ?: smartCardService.registerDistributedLocalService(
                LocalServiceClientFactoryBuilder.builder("localService")
                    .withSyncNode(keypleSyncEndPointClient)
                    .build())
    return localService.getExtension(LocalServiceClient::class.java)
  }

  @Provides
  @AppScoped
  fun provideReaderRepository(): ReaderRepository {
    return ReaderRepository
  }
}
