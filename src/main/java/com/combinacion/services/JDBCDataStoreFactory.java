package com.combinacion.services;

import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.combinacion.util.DBConnection;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Almacena el token de Google OAuth en la base de datos (tabla google_oauth_token).
 */
public class JDBCDataStoreFactory extends AbstractDataStoreFactory {

    @Override
    protected <V extends Serializable> DataStore<V> createDataStore(String id) throws IOException {
        return new JDBCDataStore<V>(this, id);
    }

    static class JDBCDataStore<V extends Serializable> extends AbstractDataStore<V> {

        public JDBCDataStore(DataStoreFactory dataStoreFactory, String id) {
            super(dataStoreFactory, id);
        }

        private String serializeToBase64(V obj) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(obj);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }

        @SuppressWarnings("unchecked")
        private V deserializeFromBase64(String base64) throws IOException {
            if (base64 == null || base64.isEmpty()) return null;
            byte[] bytes = Base64.getDecoder().decode(base64);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                return (V) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("Error deserializing token", e);
            }
        }

        @Override
        public Set<String> keySet() throws IOException {
            return Collections.singleton("user");
        }

        @Override
        public Collection<V> values() throws IOException {
            V val = get("user");
            if (val == null) return Collections.emptyList();
            return Collections.singletonList(val);
        }

        @Override
        public V get(String key) throws IOException {
            String fullKey = getId() + "_" + key;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT token_data FROM google_oauth_token WHERE id = ?")) {
                stmt.setString(1, fullKey);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return deserializeFromBase64(rs.getString("token_data"));
                    }
                }
            } catch (SQLException e) {
                throw new IOException("DB Error", e);
            }
            return null;
        }

        @Override
        public DataStore<V> set(String key, V value) throws IOException {
            String fullKey = getId() + "_" + key;
            String base64Data = serializeToBase64(value);

            try (Connection conn = DBConnection.getConnection()) {
                boolean exists = false;
                try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM google_oauth_token WHERE id = ?")) {
                    check.setString(1, fullKey);
                    try (ResultSet rs = check.executeQuery()) {
                        exists = rs.next();
                    }
                }
                if (exists) {
                    try (PreparedStatement update = conn.prepareStatement("UPDATE google_oauth_token SET token_data = ? WHERE id = ?")) {
                        update.setString(1, base64Data);
                        update.setString(2, fullKey);
                        update.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO google_oauth_token (id, token_data) VALUES (?, ?)")) {
                        insert.setString(1, fullKey);
                        insert.setString(2, base64Data);
                        insert.executeUpdate();
                    }
                }
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
            } catch (SQLException e) {
                throw new IOException("DB Error", e);
            }
            return this;
        }

        @Override
        public DataStore<V> clear() throws IOException {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM google_oauth_token WHERE id LIKE ?")) {
                stmt.setString(1, getId() + "_%");
                stmt.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            } catch (SQLException e) {
                throw new IOException("DB Error", e);
            }
            return this;
        }

        @Override
        public DataStore<V> delete(String key) throws IOException {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM google_oauth_token WHERE id = ?")) {
                stmt.setString(1, getId() + "_" + key);
                stmt.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            } catch (SQLException e) {
                throw new IOException("DB Error", e);
            }
            return this;
        }
    }
}
