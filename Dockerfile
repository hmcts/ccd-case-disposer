ARG APP_INSIGHTS_AGENT_VERSION=3.6.2

FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ccd-case-disposer.jar /opt/app/

EXPOSE 4458
CMD [ "ccd-case-disposer.jar" ]
