package com.edusuite.platform.tenant;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TenantAwareConnectionProviderTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final TenantAwareConnectionProvider provider = new TenantAwareConnectionProvider(dataSource);

    @Test
    void getAnyConnectionReturnsPlainDataSourceConnection() throws Exception {
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        Connection result = provider.getAnyConnection();

        assertThat(result).isSameAs(connection);
        verify(dataSource).getConnection();
        verifyNoInteractions(connection);
    }

    @Test
    void releaseAnyConnectionClosesConnection() throws Exception {
        Connection connection = mock(Connection.class);

        provider.releaseAnyConnection(connection);

        verify(connection).close();
    }

    @Test
    void getConnectionAppliesTenantSetting() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT set_config('app.current_tenant', ?, true)")).thenReturn(ps);

        Connection result = provider.getConnection("tenant-123");

        assertThat(result).isSameAs(connection);
        verify(ps).setString(1, "tenant-123");
        verify(ps).execute();
        verify(ps).close();
    }

    @Test
    void getConnectionMapsNoTenantToEmptyString() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT set_config('app.current_tenant', ?, true)")).thenReturn(ps);

        provider.getConnection(CurrentTenantIdentifierResolverImpl.NO_TENANT);

        verify(ps).setString(1, "");
    }

    @Test
    void releaseConnectionClearsTenantAndClosesConnection() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT set_config('app.current_tenant', '', false)")).thenReturn(ps);

        provider.releaseConnection("tenant-123", connection);

        verify(ps).execute();
        verify(ps).close();
        verify(connection).close();
    }

    @Test
    void supportsAggressiveReleaseReturnsFalse() {
        assertThat(provider.supportsAggressiveRelease()).isFalse();
    }

    @Test
    void isUnwrappableAsReturnsFalse() {
        assertThat(provider.isUnwrappableAs(DataSource.class)).isFalse();
    }

    @Test
    void unwrapThrowsUnsupportedOperationException() {
        assertThatThrownBy(() -> provider.unwrap(DataSource.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("does not support unwrapping");
    }
}
