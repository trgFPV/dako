package edu.hm.dako.chat.AuditLogServer;

public interface GenericServerInterface {

  /**
   * Start the server
   */
  void start();

  /**
   * Strop the server
   *
   * @throws Exception
   */
  void stop() throws Exception;
}

