package de.fraunhofer.fokus.ids.persistence.managers;

import de.fraunhofer.fokus.ids.models.Constants;
import de.fraunhofer.fokus.ids.persistence.entities.DataSource;
import de.fraunhofer.fokus.ids.persistence.enums.DatasourceType;
import de.fraunhofer.fokus.ids.persistence.service.DatabaseService;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Date;

import static de.fraunhofer.fokus.ids.persistence.util.Functions.checkNull;

public class DataSourceManager {

    DatabaseService dbService;
    private Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class.getName());


    public DataSourceManager(Vertx vertx) {
        dbService = DatabaseService.createProxy(vertx, Constants.DATABASE_SERVICE);
    }

    public void update(DataSource dataSource, Handler<AsyncResult<Void>> resultHandler) {

        String update = "UPDATE  DataSource SET  updated_at = ?, datasourcename = ?, data = ?, datasourcetype = ?" +
                "WHERE id = ?";
        Date d = new Date();
        JsonArray params = new JsonArray()
                .add(d.toInstant())
                .add(checkNull(dataSource.getDatasourceName()))
                .add(dataSource.getData())
                .add(dataSource.getDatasourceType().ordinal())
                .add(dataSource.getId());

        dbService.update(update, params, reply -> {
            if (reply.failed()) {
                LOGGER.info(reply.cause());
                resultHandler.handle(Future.failedFuture(reply.cause().toString()));
            } else {
                resultHandler.handle(Future.succeededFuture());
            }
        });

    }

    public void add(DataSource dataSource, Handler<AsyncResult<Void>> resultHandler) {

        String update = "INSERT INTO DataSource (created_at, updated_at, datasourcename, data, datasourcetype) " +
                "values (?, ?, ?, ?, ?)";
        Date d = new Date();
        JsonArray params = new JsonArray()
                .add(d.toInstant())
                .add(d.toInstant())
                .add(checkNull(dataSource.getDatasourceName()))
                .add(dataSource.getData().toString())
                .add(dataSource.getDatasourceType().ordinal());

        dbService.update(update, params, reply -> {
            if (reply.failed()) {
                LOGGER.info(reply.cause());
                resultHandler.handle(Future.failedFuture(reply.cause().toString()));
            } else {
                resultHandler.handle(Future.succeededFuture());
            }
        });
    }

    public void delete(Long id, Handler<AsyncResult<Void>> resultHandler) {
        dbService.update("DELETE FROM datasource WHERE id= ?",new JsonArray().add(id), reply -> {
            if (reply.failed()) {
                LOGGER.info(reply.cause());
                resultHandler.handle(Future.failedFuture(reply.cause().toString()));
            } else {
                resultHandler.handle(Future.succeededFuture());
            }
        });
    }

    public void findByType(DatasourceType type, Handler<AsyncResult<JsonArray>> resultHandler) {
        dbService.query("SELECT * FROM DataSource WHERE datasourcetype=?",new JsonArray().add(type.ordinal()), reply -> {
            if (reply.failed()) {
                LOGGER.info(reply.cause());
                resultHandler.handle(Future.failedFuture(reply.cause().toString()));
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonArray(reply.result())));
            }
        });
    }

    public void findById(Long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        dbService.query("SELECT * FROM DataSource WHERE id=?",new JsonArray().add(id), reply -> {
            if (reply.failed()) {
                LOGGER.info(reply.cause());
                resultHandler.handle(Future.failedFuture(reply.cause().toString()));
            } else {
                resultHandler.handle(Future.succeededFuture(reply.result().get(0)));
            }
        });
    }

    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        dbService.query("SELECT * FROM DataSource ORDER BY id DESC" ,new JsonArray(), reply -> {
            if (reply.failed()) {
                LOGGER.info(reply.cause());
                resultHandler.handle(Future.failedFuture(reply.cause().toString()));
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonArray(reply.result())));
            }
        });
    }

    public void findAllByType(Handler<AsyncResult<JsonObject>> resultHandler) {
        dbService.query( "SELECT * FROM DataSource ORDER BY datasourcetype" ,new JsonArray(), reply -> {
            if (reply.failed()) {
                LOGGER.info(reply.cause());
                resultHandler.handle(Future.failedFuture(reply.cause().toString()));
            } else {

                JsonObject res = new JsonObject();
                for(DatasourceType t : DatasourceType.values()){
                    res.put(t.name(), new JsonArray());
                }
                for(JsonObject jsonObject : reply.result()){
                    String currentType = DatasourceType.values()[jsonObject.getLong("datasourcetype").intValue()].name();
                    res.getJsonArray(currentType).add(jsonObject);
                }
                resultHandler.handle(Future.succeededFuture(res));
            }
        });
    }

}