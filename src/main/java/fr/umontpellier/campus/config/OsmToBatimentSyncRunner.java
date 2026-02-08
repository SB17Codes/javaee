package fr.umontpellier.campus.config;

import fr.umontpellier.campus.service.OsmToBatimentSyncService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OsmToBatimentSyncRunner implements ApplicationRunner {
  private final OsmToBatimentSyncService syncService;

  public OsmToBatimentSyncRunner(OsmToBatimentSyncService syncService) {
    this.syncService = syncService;
  }

  @Override
  public void run(ApplicationArguments args) {
    syncService.sync();
  }
}
