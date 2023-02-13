ARG APP_INSIGHTS_AGENT_VERSION=3.2.4

# Application image
ARG PLATFORM=""

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ccd-case-disposer.jar /opt/app/

EXPOSE 4458
CMD [ "ccd-case-disposer.jar" ]
