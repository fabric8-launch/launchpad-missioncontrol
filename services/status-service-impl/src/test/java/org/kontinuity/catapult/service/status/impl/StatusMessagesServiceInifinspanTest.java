package org.kontinuity.catapult.service.status.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.infinispan.Cache;
import org.junit.Rule;
import org.junit.Test;
import org.kontinuity.catapult.core.api.StatusMessage;
import org.kontinuity.catapult.core.api.StatusMessageEvent;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test to see if {@link StatusMessagesServiceInifinspan} works
 */
public class StatusMessagesServiceInifinspanTest {

    @Mock
    Cache<UUID, List<StatusMessageEvent>> cacheMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void shouldAddToListWhenPresentInCache() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        StatusMessagesServiceInifinspan victim = new StatusMessagesServiceInifinspan(cacheMock);
        List<StatusMessageEvent> statusMessageEvents = new ArrayList<>();

        //when
        when(cacheMock.get(id)).thenReturn(statusMessageEvents);
        StatusMessageEvent msg = new StatusMessageEvent(id, StatusMessage.GITHUB_CREATE);
        victim.onEvent(msg);

        //then
        assertEquals(statusMessageEvents, victim.getStatusMessages(id));
        verify(cacheMock).put(id, statusMessageEvents);
    }

    @Test
    public void shouldAddToCacheWhenNotPresent() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        StatusMessagesServiceInifinspan victim = new StatusMessagesServiceInifinspan(cacheMock);

        //when
        when(cacheMock.get(id)).thenReturn(null);

        StatusMessageEvent msg = new StatusMessageEvent(id, StatusMessage.GITHUB_CREATE);

        when(cacheMock.put(eq(id), any())).thenAnswer(invocationOnMock -> {
            //then
            assertTrue(invocationOnMock.getArgument(1) instanceof List);
            List<StatusMessageEvent> statusMessageEvents = invocationOnMock.getArgument(1);
            assertTrue(statusMessageEvents.contains(msg));
            return null;
        });

        victim.onEvent(msg);
    }
}