package atom.service;

import java.util.Iterator;
import java.util.List;
import atom.Atom;
import atom.Startable;

/**
 * Service used to initialize other services at Atom startup
 */
public class InitialService implements Startable {

  // the list of services to initialize
  private List<String> mInitialServices;


  /**
   * No-args constructor
   */
  public InitialService () {
    //
  }


  /**
   * Initializes each configured service
   */
  public void start () {
    if (mInitialServices != null && !mInitialServices.isEmpty()) {
      for (Object initialService : mInitialServices) {
        String serviceName = (String) initialService;
        if (serviceName != null && !"".equals(serviceName)) {
          Atom.getAtom().resolveName(serviceName);
        }
      }
    }
  }


  public List<String> getInitialServices () {
    return mInitialServices;
  }

  public void setInitialServices (List<String> pInitialServices) {
    mInitialServices = pInitialServices;
  }

} // end InitialService

